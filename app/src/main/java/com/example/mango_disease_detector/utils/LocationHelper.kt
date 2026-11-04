package com.example.mango_disease_detector.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Approximate location using the platform LocationManager (no Google Play Services needed).
 * Only ACCESS_COARSE_LOCATION is used: district-level accuracy is enough for weather.
 */
object LocationHelper {
    private const val TIMEOUT_MS = 10_000L
    private const val MAX_LAST_KNOWN_AGE_MS = 30L * 60 * 1000

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    /** Returns a recent location, or null if location is off, denied, or times out. */
    suspend fun getLocation(context: Context): Location? {
        if (!hasPermission(context)) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        lastKnown(lm)?.let { last ->
            if (System.currentTimeMillis() - last.time < MAX_LAST_KNOWN_AGE_MS) return last
        }

        val providers = usableProviders(lm)
        if (providers.isEmpty()) return lastKnown(lm)
        // Ask all providers at once; the first fix wins. A single provider (e.g. network on a
        // phone without network location) may never answer.
        return withTimeoutOrNull(TIMEOUT_MS) { firstLocation(context, lm, providers) } ?: lastKnown(lm)
    }

    private fun usableProviders(lm: LocationManager): List<String> {
        val candidates = mutableListOf(LocationManager.NETWORK_PROVIDER)
        // Since Android 12 coarse-only apps may use GPS/fused (results are coarsened by the OS).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            candidates += LocationManager.FUSED_PROVIDER
            candidates += LocationManager.GPS_PROVIDER
        }
        return candidates.filter {
            try {
                lm.isProviderEnabled(it)
            } catch (e: Exception) {
                false
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun lastKnown(lm: LocationManager): Location? =
        lm.getProviders(true).mapNotNull {
            try {
                lm.getLastKnownLocation(it)
            } catch (e: SecurityException) {
                null
            }
        }.maxByOrNull { it.time }

    /**
     * Requests one fix from every provider in parallel. Resumes with the first non-null
     * location, or null once every provider has failed. All callbacks run on the main thread.
     */
    @SuppressLint("MissingPermission")
    private suspend fun firstLocation(
        context: Context,
        lm: LocationManager,
        providers: List<String>
    ): Location? = suspendCancellableCoroutine { cont ->
        val signals = mutableListOf<CancellationSignal>()
        val listeners = mutableListOf<LocationListener>()
        var pending = providers.size

        fun cleanup() {
            signals.forEach { it.cancel() }
            listeners.forEach { lm.removeUpdates(it) }
        }

        fun onResult(loc: Location?) {
            if (!cont.isActive) return
            pending--
            if (loc != null) {
                cleanup()
                cont.resume(loc)
            } else if (pending <= 0) {
                cont.resume(null)
            }
        }

        cont.invokeOnCancellation { ContextCompat.getMainExecutor(context).execute { cleanup() } }

        for (provider in providers) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val signal = CancellationSignal().also { signals += it }
                    lm.getCurrentLocation(provider, signal, ContextCompat.getMainExecutor(context)) { onResult(it) }
                } else {
                    var done = false
                    val listener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            if (!done) { done = true; onResult(location) }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
                        override fun onProviderEnabled(provider: String) = Unit
                        override fun onProviderDisabled(provider: String) {
                            if (!done) { done = true; onResult(null) }
                        }
                    }
                    listeners += listener
                    @Suppress("DEPRECATION")
                    lm.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                }
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }
}
