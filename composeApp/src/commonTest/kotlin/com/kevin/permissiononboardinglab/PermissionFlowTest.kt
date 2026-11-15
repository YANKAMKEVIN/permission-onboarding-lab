package com.kevin.permissiononboardinglab

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PermissionFlowTest {

    private fun run(vararg actions: PermissionAction) =
        actions.fold(PermissionFlowState()) { s, a -> reduce(s, a) }

    @Test
    fun happyPathReachesGranted() {
        val s = run(PermissionAction.Primary, PermissionAction.Primary, PermissionAction.SystemAllow)
        assertEquals(PermissionStep.Granted, s.step)
    }

    @Test
    fun notNowBeforeTheDialogIsASoftRefusal() {
        val s = run(PermissionAction.Primary, PermissionAction.Secondary)
        assertEquals(PermissionStep.Denied, s.step)
        assertEquals(0, s.denials)
    }

    @Test
    fun firstSystemDenialCanBeRetried() {
        val s = run(PermissionAction.Primary, PermissionAction.Primary, PermissionAction.SystemDeny)
        assertEquals(PermissionStep.Denied, s.step)
        assertEquals(PermissionStep.SystemAsk, reduce(s, PermissionAction.Primary).step)
    }

    @Test
    fun secondSystemDenialBlocksAndRoutesToSettings() {
        val blocked = run(
            PermissionAction.Primary, PermissionAction.Primary, PermissionAction.SystemDeny,
            PermissionAction.Primary, PermissionAction.SystemDeny
        )
        assertEquals(PermissionStep.Blocked, blocked.step)
        assertEquals("Open settings", primaryLabel(blocked))

        val settings = reduce(blocked, PermissionAction.Primary)
        assertTrue(settings.settingsOpen)
        assertEquals("Turn on", primaryLabel(settings))
        assertFalse(reduce(settings, PermissionAction.Secondary).settingsOpen)

        val granted = reduce(settings, PermissionAction.Primary)
        assertEquals(PermissionStep.Granted, granted.step)
        assertFalse(granted.settingsOpen)
    }

    @Test
    fun selectedOnlyGivesLimitedAccess() {
        val s = run(PermissionAction.Primary, PermissionAction.Primary, PermissionAction.SystemAllowSelected)
        assertEquals(PermissionStep.Limited, s.step)
    }

    @Test
    fun systemActionsAreIgnoredOutsideTheDialog() {
        val s = PermissionFlowState()
        assertEquals(s, reduce(s, PermissionAction.SystemAllow))
        assertEquals(s, reduce(s, PermissionAction.SystemDeny))
    }

    @Test
    fun jumpToBlockedPresetsDenialsAndGrantedHasNoSecondary() {
        assertEquals(DENIALS_BEFORE_BLOCK, reduce(PermissionFlowState(), PermissionAction.JumpTo(PermissionStep.Blocked)).denials)
        assertNull(secondaryLabel(PermissionFlowState(step = PermissionStep.Granted)))
        assertEquals(PermissionFlowState(), reduce(PermissionFlowState(step = PermissionStep.Granted), PermissionAction.Primary))
    }
}
