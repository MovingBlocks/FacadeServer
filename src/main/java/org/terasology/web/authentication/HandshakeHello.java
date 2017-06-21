/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.web.authentication;

import com.google.common.primitives.Bytes;
import org.terasology.identity.PublicIdentityCertificate;
import org.terasology.web.io.gsonUtils.InvalidClientMessageException;
import org.terasology.web.io.gsonUtils.Validable;

import java.nio.ByteBuffer;

public class HandshakeHello implements Validable {

    private byte[] random;
    private PublicIdentityCertificate certificate;
    private byte[] timestamp;

    public HandshakeHello(byte[] random, PublicIdentityCertificate certificate, long timestamp) {
        this.random = random;
        this.certificate = certificate;
        this.timestamp = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(timestamp).array();
    }

    public PublicIdentityCertificate getCertificate() {
        return certificate;
    }

    private byte[] certificateToByteArray() {
        return Bytes.concat(certificate.getId().getBytes(), certificate.getModulus().toByteArray(),
                certificate.getExponent().toByteArray(), certificate.getSignatureBytes());
    }

    private byte[] toByteArray() {
        return Bytes.concat(random, certificateToByteArray(), timestamp);
    }

    public static byte[] concat(HandshakeHello a, HandshakeHello b) {
        return Bytes.concat(a.toByteArray(), b.toByteArray());
    }

    @Override
    public void validate() throws InvalidClientMessageException {
        if (random == null || certificate == null || timestamp == null) {
            throw new InvalidClientMessageException("random, certificate and timestamp fields must be specified");
        }
        if (certificate.getId() == null || certificate.getModulus() == null
                || certificate.getExponent() == null || certificate.getSignature() == null) {
            throw new InvalidClientMessageException("the certificate must contain the id, modulus, exponent and signature fields");
        }
    }
}
