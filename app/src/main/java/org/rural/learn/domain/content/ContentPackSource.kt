package org.rural.learn.domain.content
interface ContentPackSource {
 suspend fun install(pack: Pack)
 suspend fun storageBytes(): Long
}
object PackVerification {
 fun verify(bytes: ByteArray, pack: Pack): Boolean = bytes.size.toLong()==pack.sizeBytes && java.security.MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }==pack.sha256
}
