package com.example.forcedsocial.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.forcedsocial.auth.AuthViewModel
import com.example.forcedsocial.components.CommentInput
import com.example.forcedsocial.components.PostCard
import com.example.forcedsocial.models.Comment
import com.example.forcedsocial.models.Post
import com.example.forcedsocial.models.User
import com.example.forcedsocial.viewmodels.CommentViewModel
import com.example.forcedsocial.viewmodels.PostViewModel
import com.example.forcedsocial.viewmodels.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun PostDiscussion(postId: String, navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val postViewModel: PostViewModel = viewModel()
    val commentViewModel: CommentViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val currentUser by authViewModel.currentUser.observeAsState()

    var post by remember { mutableStateOf<Post?>(null) }
    var comments by remember { mutableStateOf<List<Comment>?>(null) }
    var postUser by remember { mutableStateOf<User?>(null) }
    val commentUsers = remember { mutableStateMapOf<String, User>() }

    LaunchedEffect(postId) {
        if (postId.isNotEmpty()) {
            postViewModel.getPostById(postId).addOnSuccessListener {
                post = it.toObject(Post::class.java)
                post?.userId?.let { userId ->
                    userViewModel.getUserProfile(userId).addOnSuccessListener { document ->
                        postUser = document.toObject(User::class.java)
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(
                    context,
                    "Couldn't get the post",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    LaunchedEffect(postId) {
        if (postId.isNotEmpty()) {
            commentViewModel.getRealTimeCommentsForPost(postId) { updatedComments ->
                comments = updatedComments
                updatedComments.forEach { comment ->
                    userViewModel.getUserProfile(comment.userId).addOnSuccessListener { document ->
                        commentUsers[comment.userId] = document.toObject(User::class.java) ?: User()
                    }
                }
            }
        }
    }

    BottomNavigationLayout(
        navController,
        authViewModel
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn {
                item {
                    post?.let {
                        PostCard(
                            userName = postUser?.displayName ?: it.userId,
                            postText = it.content,
                            userImageUri = postUser?.profilePictureUrl?.let { url -> Uri.parse(url) },
                            onClick = {}
                        )
                    }

                    CommentInput(
                        userName = currentUser?.displayName ?: "",
                        onCommentSubmit = { commentText ->
                            commentViewModel.createComment(
                                userId = currentUser?.uid ?: "",
                                postId = postId,
                                content = commentText,
                                imageUrl = null
                            )
                        }
                    )

                    comments?.forEach { comment ->
                        PostCard(
                            userName = commentUsers[comment.userId]?.displayName ?: comment.userId,
                            postText = comment.content,
                            userImageUri = commentUsers[comment.userId]?.profilePictureUrl?.let { url ->
                                Uri.parse(
                                    url
                                )
                            },
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}