package com.arnyminerz.paraulogic.play.games

import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.snapshot.Snapshot
import com.google.android.gms.games.snapshot.SnapshotMetadata
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val MAX_SNAPSHOT_RESOLVE_RETRIES = 10

private fun processSnapshotOpenResult(
    snapshotsClient: SnapshotsClient,
    result: SnapshotsClient.DataOrConflict<Snapshot>,
    retryCount: Int
): Task<Snapshot> {
    if (!result.isConflict) {
        val source = TaskCompletionSource<Snapshot>()
        source.setResult(result.data)
        return source.task
    }

    // There was a conflict.  Try resolving it by selecting the newest of the conflicting snapshots.
    // This is the same as using RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED as a conflict resolution
    // policy, but we are implementing it as an example of a manual resolution.
    // One option is to present a UI to the user to choose which snapshot to resolve.
    val conflict = result.conflict!!

    val snapshot = conflict.snapshot
    val conflictSnapshot = conflict.conflictingSnapshot

    // Resolve between conflicts by selecting the newest of the conflicting snapshots.
    var resolvedSnapshot = snapshot

    if (snapshot.metadata.lastModifiedTimestamp < conflictSnapshot.metadata.lastModifiedTimestamp)
        resolvedSnapshot = conflictSnapshot

    return snapshotsClient
        .resolveConflict(conflict.conflictId, resolvedSnapshot)
        .continueWithTask { task: Task<SnapshotsClient.DataOrConflict<Snapshot>> ->
            // Resolving the conflict may cause another conflict,
            // so recurse and try another resolution.
            if (retryCount < MAX_SNAPSHOT_RESOLVE_RETRIES)
                processSnapshotOpenResult(snapshotsClient, task.result, retryCount + 1)
            else
                throw Exception("Could not resolve snapshot conflicts")
        }
}

/**
 * Loads the saved game snapshot from Google Play Games.
 * @author Arnau Mora
 * @since 20220317
 * @param snapshotsClient The snapshots client to load the data from.
 */
@WorkerThread
suspend fun loadSnapshot(snapshotsClient: SnapshotsClient): Snapshot {
    Timber.v("Getting snapshot client...")
    val conflictResolutionPolicy = SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED

    Timber.v("Opening snapshot...")
    val result = snapshotsClient
        .open("Paraulogic", true, conflictResolutionPolicy)
        .await()

    Timber.v("Processing snapshot open result...")
    return processSnapshotOpenResult(snapshotsClient, result, 0).await()
}

@WorkerThread
suspend fun writeSnapshot(
    snapshotsClient: SnapshotsClient,
    snapshot: Snapshot,
    data: ByteArray,
    coverImage: Bitmap?,
    description: String,
): SnapshotMetadata = suspendCoroutine { cont ->
    snapshot.snapshotContents.writeBytes(data)

    val metadataChange = SnapshotMetadataChange.Builder()
        .apply { coverImage?.let { setCoverImage(it) } }
        .setDescription(description)
        .build()

    snapshotsClient
        .commitAndClose(snapshot, metadataChange)
        .addOnSuccessListener { cont.resume(it) }
        .addOnFailureListener { cont.resumeWithException(it) }
}
