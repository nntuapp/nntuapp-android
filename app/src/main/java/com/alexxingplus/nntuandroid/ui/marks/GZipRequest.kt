package com.alexxingplus.nntuandroid.ui.marks

import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream


class GZipRequest : StringRequest {
    constructor(
        method: Int,
        url: String?,
        listener: Response.Listener<String?>?,
        errorListener: Response.ErrorListener?
    ) : super(method, url, listener, errorListener) {
    }

    constructor(
        url: String?,
        listener: Response.Listener<String?>?,
        errorListener: Response.ErrorListener?
    ) : super(url, listener, errorListener) {
    }

    // parse the gzip response using a GZIPInputStream
    override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
        var output: String? = ""
        try {
            val gStream = GZIPInputStream(ByteArrayInputStream(response.data))
            val reader = InputStreamReader(gStream)
            val `in` = BufferedReader(reader)
            var read: String?
            while (`in`.readLine().also { read = it } != null) {
                output += read
            }
            reader.close()
            `in`.close()
            gStream.close()
        } catch (e: IOException) {
            return Response.error(ParseError())
        }
        return Response.success(output, HttpHeaderParser.parseCacheHeaders(response))
    }
}