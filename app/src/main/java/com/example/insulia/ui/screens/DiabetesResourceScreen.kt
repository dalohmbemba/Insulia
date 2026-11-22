package com.example.insulia.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.net.http.SslError
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.insulia.R
import com.example.insulia.navigation.Screen
import com.example.insulia.ui.components.InsuliaBottomBar
import com.example.insulia.ui.components.PageHeader
import java.util.Locale

private val trustedResourceHosts = setOf(
    "diabetes.org.uk",
    "www.diabetes.org.uk",
    "nhs.uk",
    "www.nhs.uk"
)

@Composable
fun DiabetesResourceScreen(
    title: String,
    url: String,
    onBack: () -> Unit,
    onSelectTab: (Screen) -> Unit,
    selectedTab: Screen
) {
    var isLoading by remember(url) { mutableStateOf(true) }
    var loadFailed by remember(url) { mutableStateOf(false) }
    var externalLinkBlocked by remember(url) { mutableStateOf(false) }
    var webView by remember(url) { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                stopLoading()
                webChromeClient = null
                webViewClient = WebViewClient()
                destroy()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PageHeader(title = title, onBack = onBack)

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = false
                            javaScriptCanOpenWindowsAutomatically = false
                            domStorageEnabled = false
                            allowFileAccess = false
                            allowContentAccess = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                safeBrowsingEnabled = true
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                                isLoading = true
                                loadFailed = false
                            }

                            override fun onPageFinished(view: WebView, url: String) {
                                isLoading = false
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView,
                                request: WebResourceRequest
                            ): Boolean {
                                if (isTrustedResourceUrl(request.url)) return false
                                isLoading = false
                                loadFailed = true
                                externalLinkBlocked = true
                                return true
                            }

                            override fun onReceivedError(
                                view: WebView,
                                request: WebResourceRequest,
                                error: WebResourceError
                            ) {
                                if (request.isForMainFrame) {
                                    isLoading = false
                                    loadFailed = true
                                }
                            }

                            override fun onReceivedHttpError(
                                view: WebView,
                                request: WebResourceRequest,
                                errorResponse: WebResourceResponse
                            ) {
                                if (request.isForMainFrame && errorResponse.statusCode >= 400) {
                                    isLoading = false
                                    loadFailed = true
                                }
                            }

                            override fun onReceivedSslError(
                                view: WebView,
                                handler: SslErrorHandler,
                                error: SslError
                            ) {
                                handler.cancel()
                                isLoading = false
                                loadFailed = true
                            }
                        }
                        webView = this
                        if (isTrustedResourceUrl(Uri.parse(url))) {
                            loadUrl(url)
                        } else {
                            isLoading = false
                            loadFailed = true
                        }
                    }
                },
                update = { view ->
                    if (view.url != url && isTrustedResourceUrl(Uri.parse(url))) {
                        view.loadUrl(url)
                    }
                }
            )

            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.92f)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.resource_loading),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            if (loadFailed) {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(
                            if (externalLinkBlocked) R.string.resource_blocked_link
                            else R.string.resource_load_error
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            externalLinkBlocked = false
                            loadFailed = false
                            isLoading = true
                            webView?.loadUrl(url)
                        }
                    ) {
                        Text(stringResource(R.string.resource_retry))
                    }
                }
            }
        }

        InsuliaBottomBar(selected = selectedTab, onSelect = onSelectTab)
    }
}

private fun isTrustedResourceUrl(uri: Uri): Boolean =
    uri.scheme.equals("https", ignoreCase = true) &&
        uri.host?.lowercase(Locale.ROOT)?.let { it in trustedResourceHosts } == true
