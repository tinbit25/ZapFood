package com.example.food.ui.screens.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.core.util.Resource
import com.example.food.ui.viewmodel.FeedbackViewModel
import com.example.food.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    orderId: String? = null,
    onNavigateBack: () -> Unit,
    userViewModel: UserViewModel,
    feedbackViewModel: FeedbackViewModel = viewModel()
) {
    val user by userViewModel.user.collectAsState()
    
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var submitState by remember { mutableStateOf<Resource<Unit>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave Feedback") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("How was your experience?", style = MaterialTheme.typography.titleLarge)
            
            // Star rating simulation
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 1..5) {
                    Button(
                        onClick = { rating = i },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (i <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (i <= rating) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("$i")
                    }
                }
            }
            Text("Rating: $rating / 5", style = MaterialTheme.typography.bodyLarge)

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Any comments or suggestions?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            if (submitState is Resource.Loading) {
                CircularProgressIndicator()
            }
            if (submitState is Resource.Error) {
                Text(
                    text = (submitState as Resource.Error).message ?: "Error",
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (submitState is Resource.Success) {
                Text("Feedback submitted successfully!", color = MaterialTheme.colorScheme.primary)
            }

            Button(
                onClick = {
                    user?.let { u ->
                        submitState = Resource.Loading()
                        feedbackViewModel.submitFeedback(u, rating, comment, orderId) {
                            submitState = it
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = submitState !is Resource.Loading && submitState !is Resource.Success
            ) {
                Text("Submit Feedback")
            }
        }
    }
}
