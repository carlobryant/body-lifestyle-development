package com.example.bold_app.google_services

import android.app.Activity
import android.content.Context
import androidx.activity.viewModels
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.ViewModelProvider
import com.example.bold_app.R
import com.example.bold_app.viewmodel.LogInViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException
import kotlin.getValue

class GoogleAuthClient(
    private val context: Context,
) {
    private val tag = "GoogleAuthClient: "

    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    /*fun isSignedIn(): Boolean {
        if (firebaseAuth.currentUser != null){
            println(tag + "Already signed in")
            return true
        }
        return false
    }
    suspend fun signIn(): Boolean {
        if (isSignedIn()) return true

        try {
            val result = buildCredentialRequest(activity)
            return handleSignIn(result)

        } catch (e: Exception){
            e.printStackTrace()
            if (e is CancellationException) throw e
            println(tag + "Sign In Error ${e.message}")
            return  false
        }
    }*/

    suspend fun signIn(activity: Activity): Boolean {
        // Use the activity context here
        //if (isSignedIn()) return true
        try {
            val result = buildCredentialRequest(activity)
            return handleSignIn(result)

        } catch (e: Exception){
            e.printStackTrace()
            if (e is CancellationException) throw e
            println(tag + "Sign In Error ${e.message}")
            return  false
        }

    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        firebaseAuth.signOut()
    }

    private suspend fun buildCredentialRequest(activity: Activity): GetCredentialResponse {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(activity.getString(R.string.default_web_client_id)) // Use activity
                    .setAutoSelectEnabled(false)
                    .build()
            ).build()
        return CredentialManager.create(activity).getCredential(
            request = request,
            context = activity
        )
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): Boolean {
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                println(tag + "name: ${tokenCredential.displayName}")
                println(tag + "email: ${tokenCredential.id}")
                println(tag + "image: ${tokenCredential.profilePictureUri}")

                val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()

                return authResult.user != null

            } catch (e: GoogleIdTokenParsingException) {
                println(tag + "Sign In Error ${e.message}")
                return  false
            }
        } else {
            println(tag + "credential is not GoogleIdTokenCredential")
            return false
        }
    }
}