package org.rural.learn
import org.junit.Assert.*
import org.junit.Test
import org.rural.learn.domain.content.*
class PackVerificationTest { @Test fun rejectsCorruptionAndWrongSize() { val b="hello".toByteArray(); val p=Pack("p","en","math",1,5,"2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824","x",false); assertTrue(PackVerification.verify(b,p)); assertFalse(PackVerification.verify("Hello".toByteArray(),p)); assertFalse(PackVerification.verify(b,p.copy(sizeBytes=4))) } }
