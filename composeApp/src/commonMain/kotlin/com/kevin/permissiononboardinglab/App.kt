package com.kevin.permissiononboardinglab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Teal,
            secondary = Gold,
            background = Paper,
            surface = Color.White
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Paper
        ) {
            PermissionOnboardingLab()
        }
    }
}

private data class StepCopy(
    val step: PermissionStep,
    val tab: String,
    val title: String,
    val body: String,
    val rule: String,
    val accent: Color
)

private val Paper = Color(0xFFF5F0E7)
private val Ink = Color(0xFF111713)
private val Muted = Color(0xFF68736D)
private val Line = Color(0xFFE1D8CB)
private val Teal = Color(0xFF0E7059)
private val Blue = Color(0xFF2864A8)
private val Gold = Color(0xFFB66F12)
private val Violet = Color(0xFF7354B8)
private val Red = Color(0xFFD34A42)
private val Slate = Color(0xFF34444B)
private val Brick = Color(0xFF9A3D2C)

private val steps = listOf(
    StepCopy(
        step = PermissionStep.Value,
        tab = "Value",
        title = "Show value before asking",
        body = "Let users understand the feature before the OS dialog appears.",
        rule = "The best permission request often starts before the permission request.",
        accent = Teal
    ),
    StepCopy(
        step = PermissionStep.Rationale,
        tab = "Why",
        title = "Explain the tradeoff",
        body = "Connect the permission to a concrete benefit and keep the promise specific.",
        rule = "A rationale should clarify, not pressure.",
        accent = Blue
    ),
    StepCopy(
        step = PermissionStep.SystemAsk,
        tab = "Ask",
        title = "Time the system prompt",
        body = "Ask when intent is fresh, not at app launch before context exists.",
        rule = "System prompts work best after a clear user action.",
        accent = Gold
    ),
    StepCopy(
        step = PermissionStep.Denied,
        tab = "Denied",
        title = "Respect the refusal",
        body = "Offer a path forward and explain what is unavailable without blame.",
        rule = "A denied state is still part of the product experience.",
        accent = Red
    ),
    StepCopy(
        step = PermissionStep.Limited,
        tab = "Limited",
        title = "Keep useful fallback paths",
        body = "Let the user continue with reduced functionality when possible.",
        rule = "Limited access is better than a dead end.",
        accent = Violet
    ),
    StepCopy(
        step = PermissionStep.Granted,
        tab = "Granted",
        title = "Confirm the unlocked value",
        body = "After permission is granted, show what changed immediately.",
        rule = "Close the loop with visible progress or newly available content.",
        accent = Slate
    ),
    StepCopy(
        step = PermissionStep.Blocked,
        tab = "Blocked",
        title = "Route to Settings",
        body = "After repeated refusals the system stops asking. Explain where to turn it on instead of re-prompting.",
        rule = "Never loop the system prompt: once it is blocked, link to Settings.",
        accent = Brick
    )
)

@Composable
private fun PermissionOnboardingLab() {
    var flow by remember { mutableStateOf(PermissionFlowState()) }
    val onAction: (PermissionAction) -> Unit = { flow = reduce(flow, it) }
    val selected = steps.first { it.step == flow.step }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 14.dp),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Header() }
        item {
            StepTabs(steps.indexOf(selected)) { onAction(PermissionAction.JumpTo(steps[it].step)) }
        }
        item {
            AnimatedContent(
                targetState = flow,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "permission-step"
            ) { shown ->
                PermissionPreview(steps.first { it.step == shown.step }, shown, onAction)
            }
        }
        item { RuleCard(selected) }
    }
}

@Composable
private fun Header() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Permission Onboarding Lab",
            color = Ink,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "A Compose flow for asking access with context, fallback and trust instead of surprise.",
            color = Muted,
            lineHeight = 23.sp,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun StepTabs(selectedIndex: Int, onSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        steps.forEachIndexed { index, step ->
            val selected = selectedIndex == index
            Surface(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelected(index) }),
                shape = RoundedCornerShape(50),
                color = if (selected) step.accent else Color.White,
                border = BorderStroke(1.dp, if (selected) step.accent else Line)
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    text = step.tab,
                    color = if (selected) Color.White else Ink,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun PermissionPreview(step: StepCopy, flow: PermissionFlowState, onAction: (PermissionAction) -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PhoneMock(step, flow, onAction)
            Text(
                text = step.title,
                color = Ink,
                fontWeight = FontWeight.Black,
                fontSize = 25.sp,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = step.body,
                color = Muted,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                secondaryLabel(flow)?.let { label ->
                    OutlinedButton(
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        onClick = { onAction(PermissionAction.Secondary) }
                    ) { Text(label) }
                }
                Button(
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    onClick = { onAction(PermissionAction.Primary) },
                    colors = ButtonDefaults.buttonColors(containerColor = step.accent),
                    shape = RoundedCornerShape(15.dp)
                ) { Text(primaryLabel(flow), fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun PhoneMock(step: StepCopy, flow: PermissionFlowState, onAction: (PermissionAction) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(step.accent.copy(alpha = 0.10f))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Line)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PermissionIcon(step)
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("Smart reminders", color = Ink, fontWeight = FontWeight.Black)
                        Text(status(flow), color = Muted, fontSize = 13.sp)
                    }
                }
                LinearProgressIndicator(
                    progress = { progress(step.step) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(50)),
                    color = step.accent,
                    trackColor = step.accent.copy(alpha = 0.13f)
                )
                when (step.step) {
                    PermissionStep.SystemAsk -> SystemPrompt(step, onAction)
                    PermissionStep.Denied -> DeniedState(step)
                    PermissionStep.Blocked -> if (flow.settingsOpen) SettingsState(step) else BlockedState(step)
                    PermissionStep.Limited -> LimitedState(step)
                    PermissionStep.Granted -> GrantedState(step)
                    else -> ValueState(step)
                }
            }
        }
    }
}

@Composable
private fun PermissionIcon(step: StepCopy) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(step.accent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (step.step == PermissionStep.Denied || step.step == PermissionStep.Blocked) {
                Icons.Filled.Close
            } else {
                Icons.Filled.Check
            },
            contentDescription = null,
            tint = step.accent
        )
    }
}

@Composable
private fun ValueState(step: StepCopy) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FeatureRow("Remind me before the deadline", step.accent)
        FeatureRow("Detect useful times automatically", step.accent)
        FeatureRow("Keep reminders private on device", step.accent)
    }
}

@Composable
private fun SystemPrompt(step: StepCopy, onAction: (PermissionAction) -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Paper,
        border = BorderStroke(1.dp, Line)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Allow reminder access?", color = Ink, fontWeight = FontWeight.Black)
            Text("Only used for timed reminders.", color = Muted, textAlign = TextAlign.Center, lineHeight = 18.sp)
            // Simulated OS dialog: each choice feeds the reducer like a real permission result.
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    onClick = { onAction(PermissionAction.SystemDeny) },
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    border = BorderStroke(1.dp, Line)
                ) {
                    Text(text = "Deny", fontSize = 13.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = Muted, fontWeight = FontWeight.Bold)
                }
                Surface(
                    onClick = { onAction(PermissionAction.SystemAllowSelected) },
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    border = BorderStroke(1.dp, Line)
                ) {
                    Text(text = "Selected", fontSize = 13.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = Ink, fontWeight = FontWeight.Bold)
                }
                Surface(
                    onClick = { onAction(PermissionAction.SystemAllow) },
                    shape = RoundedCornerShape(50),
                    color = step.accent
                ) {
                    Text(text = "Allow", fontSize = 13.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DeniedState(step: StepCopy) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FeatureRow("Manual reminders still work", step.accent)
        FeatureRow("Automatic timing is paused", step.accent)
        Text("No guilt copy. No dead end.", color = step.accent, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun BlockedState(step: StepCopy) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FeatureRow("The system will not ask again", step.accent)
        FeatureRow("Settings > Notifications > Reminders", step.accent)
    }
}

/** Simulated system Settings page: the only way out of a blocked permission. */
@Composable
private fun SettingsState(step: StepCopy) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Settings · App permissions", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Paper,
            border = BorderStroke(1.dp, Line)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Reminders", color = Ink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Off", color = step.accent, fontWeight = FontWeight.Black)
            }
        }
        Text("Use \"Turn on\" to simulate the user enabling it.", color = Muted, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun LimitedState(step: StepCopy) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FeatureRow("Use selected calendars only", step.accent)
        FeatureRow("Review access anytime in settings", step.accent)
        FeatureRow("Keep the main flow available", step.accent)
    }
}

@Composable
private fun GrantedState(step: StepCopy) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FeatureRow("3 reminders prepared", step.accent)
        FeatureRow("Next smart check: 9:00 AM", step.accent)
        FeatureRow("Access can be changed later", step.accent)
    }
}

@Composable
private fun FeatureRow(text: String, accent: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Paper,
        border = BorderStroke(1.dp, Line)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Text(text, color = Ink, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun RuleCard(step: StepCopy) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Ink
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Decision rule", color = Color.White, fontWeight = FontWeight.Black)
            Text(step.rule, color = Color(0xFFDDE4DE), lineHeight = 20.sp)
        }
    }
}

private fun status(flow: PermissionFlowState): String = when (flow.step) {
    PermissionStep.Value -> "Previewing value"
    PermissionStep.Rationale -> "Explaining why"
    PermissionStep.SystemAsk -> "System moment"
    PermissionStep.Denied -> "Permission denied"
    PermissionStep.Limited -> "Limited access"
    PermissionStep.Granted -> "Ready to use"
    PermissionStep.Blocked -> if (flow.settingsOpen) "System settings" else "Blocked by the system"
}

private fun progress(step: PermissionStep): Float = when (step) {
    PermissionStep.Value -> 0.16f
    PermissionStep.Rationale -> 0.34f
    PermissionStep.SystemAsk -> 0.52f
    PermissionStep.Denied -> 0.66f
    PermissionStep.Limited -> 0.82f
    PermissionStep.Blocked -> 0.82f
    PermissionStep.Granted -> 1f
}
