package com.tellmewhy.core.util

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.tellmewhy.BuildConfig // Replace com.example.yourapp with your actual application ID
import com.tellmewhy.domain.model.JustificationEntry
import org.json.JSONArray

fun extractMessage(jsonResponse: String?): String {
    val jsonObject = JSONObject(jsonResponse)
    return jsonObject
        .getJSONArray("choices")
        .getJSONObject(0)
        .getJSONObject("message")
        .getString("content")
}

fun JustifyAppContent(app : String, content_text : JustificationEntry): String? {

    val apiKey: String = BuildConfig.OPENROUTER_API_KEY
    val client = OkHttpClient()
    val prompt_pre = """You are 'TellMeWhy', the friendly but slightly cheeky gatekeeper of apps.  Your job: Before the user opens an app, ask them why they want to use it.  You must always respond with exactly one of these two formats: ALLOW: <short reason for approval>   DENY: <short reason for denial> which help them reflect on whether it’s truly worth their time.  You are supportive, witty, and sometimes teasing, but never mean.  If the user gives a vague reason, challenge them gently to be more specific. 
        |Rules:  - Do not output anything else apart from the chosen format.  - "ALLOW" means the content is safe and permitted.  - "DENY" means the content violates the policy or is disallowed.  - Keep the reason short (max 15 words).  - Never give explanations outside the format.""".trimMargin();

    val messagesArray = JSONArray()
        .put(JSONObject().put("role", "system").put("content", prompt_pre.trimIndent()))
        .put(JSONObject().put("role", "user").put("content", content_text.justificationText))

    val json = JSONObject()
        .put("model", "deepseek/deepseek-chat-v3-0324:free")
        .put("messages", messagesArray)
        .put("max_tokens", 60)
        .put("temperature", 0.6)

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = json.toString().toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://openrouter.ai/api/v1/chat/completions")
        .addHeader("Authorization", "Bearer $apiKey")
        .post(body)
        .build()

//    val request = Request.Builder()
//        .url("https://openrouter.ai/api/v1/chat/completions")
//        .addHeader("Authorization", "Bearer $apiKey")
//        .addHeader("Content-Type", "application/json") // Usually NOT NEEDED if RequestBody has MediaType
//        .post(body)
//        .build()

    client.newCall(request).execute().use { response ->
        if (response.isSuccessful) {
            val respBody = response.body?.string()
            println("Fine-grained verdict:\n$respBody")
            return extractMessage(respBody);
        } else {
            println("Error: HTTP ${response.code}")
        }
    }
    return "ERROR:nada";
}
