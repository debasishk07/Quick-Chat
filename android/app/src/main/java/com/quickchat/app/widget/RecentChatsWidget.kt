package com.quickchat.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.quickchat.core.database.AppDatabase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.produceState

class RecentChatsWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun appDatabase(): AppDatabase
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
            val db = entryPoint.appDatabase()
            
            val unreadChatsState = produceState<List<com.quickchat.core.database.entities.ChatEntity>>(initialValue = emptyList()) {
                value = withContext(Dispatchers.IO) {
                    try {
                        db.chatDao().getChats().filter { it.unreadCount > 0 }.take(5)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }
            val unreadChats = unreadChatsState.value

            WidgetContent(context, unreadChats)
        }
    }

    @Composable
    private fun WidgetContent(context: Context, chats: List<com.quickchat.core.database.entities.ChatEntity>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(android.graphics.Color.WHITE))
                .padding(8.dp)
        ) {
            Text(
                text = "Unread Chats",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ColorProvider(android.graphics.Color.BLACK)
                ),
                modifier = GlanceModifier.padding(bottom = 6.dp)
            )

            if (chats.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No unread messages",
                        style = TextStyle(fontSize = 12.sp, color = ColorProvider(android.graphics.Color.GRAY))
                    )
                }
            } else {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    chats.forEach { chat ->
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(
                                    actionStartActivity(
                                        Intent(context, Class.forName("com.quickchat.app.MainActivity")).apply {
                                            putExtra("navigate_to_chat", chat.recipientPhone)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        }
                                    )
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Text(
                                    text = chat.displayName,
                                    style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, color = ColorProvider(android.graphics.Color.BLACK))
                                )
                                Text(
                                    text = "${chat.unreadCount} unread",
                                    style = TextStyle(fontSize = 11.sp, color = ColorProvider(android.graphics.Color.parseColor("#4caf50")))
                                )
                            }
                            Text(
                                text = "Open",
                                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorProvider(android.graphics.Color.parseColor("#6366f1")))
                            )
                        }
                    }
                }
            }
        }
    }
}

class RecentChatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RecentChatsWidget()
}
