package com.example.forcedsocial.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.forcedsocial.auth.AuthViewModel
import com.example.forcedsocial.components.InputTextField
import com.example.forcedsocial.components.PostCard
import com.example.forcedsocial.models.Post
import com.example.forcedsocial.models.User
import com.example.forcedsocial.viewmodels.SearchViewModel
import com.example.forcedsocial.viewmodels.UserViewModel

@Composable
fun SearchScreen(authViewModel: AuthViewModel, navController: NavController) {
    val userViewModel: UserViewModel = viewModel()
    val searchViewModel: SearchViewModel = viewModel()
    val query = remember { mutableStateOf("") }
    val searchResults by searchViewModel.searchResults.collectAsState()

    BottomNavigationLayout(navController, authViewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InputTextField(
                    label = "Search",
                    onTextChange = { query.value = it }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    searchViewModel.searchPosts(query.value)
                }) {
                    Text(text = "Search")
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(searchResults) { post ->
                        val userId = post.userId
                        val user = remember {
                            mutableStateOf<User?>(null)
                        }

                        userViewModel.getUserProfile(userId).addOnSuccessListener {
                            user.value = it.toObject(User::class.java)
                        }.addOnFailureListener {
                            Log.e("Post User", "Error while getting User from post")
                        }

                        PostCard(
                            userName = if (!user.value?.displayName.isNullOrEmpty()) user.value?.displayName?: "" else post.userId,
                            postText = post.content,
                            userImageUri = if (!user.value?.profilePictureUrl.isNullOrEmpty()) Uri.parse(user.value?.profilePictureUrl) else null,
                            postImageUri = if (!post.imageUrl.isNullOrEmpty()) Uri.parse(post.imageUrl) else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Post) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(text = "Content: ${post.content}")
        Text(text = "User ID: ${post.userId}")
        post.imageUrl?.let {
            Text(text = "Image URL: $it")
        }
        post.timestamp?.let {
            Text(text = "Timestamp: ${it.toDate()}")
        }
    }
}