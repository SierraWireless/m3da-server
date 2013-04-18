/*******************************************************************************
 * Copyright (c) 2013 Sierra Wireless.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 ******************************************************************************/
package m3da.codec;

import java.security.KeyPair;

/**
 * ECC Diffie-Hellman service.
 * 
 * Implement Diffie-Hellman key negotiation for generating secure shared secrets.
 */
public interface EcdhService {

    /**
     * Generate a Elliptic curve Diffie–Hellman key pair (one public, one private)
     */
    KeyPair generateEcdhKeyPair();

    /**
     * Extract the public certificate to be sent to the remote pair.
     */
    byte[] getPublicKeyCertificate(KeyPair aKeyPair);

    /**
     * Produce the shared secret from your key pair and the other side public key x963 certificate.
     * 
     * @param yourKeyPair your private/public ECDH key pair
     * @param x963Cert the other side (remote device) public ECDH key X.963 certificate
     * @return the shared secret
     */
    byte[] computeSharedSecret(KeyPair yourKeyPair, byte[] x963Cert);

    /**
     * xor cipher the payload using the given ECCDH secret
     * 
     * @param secret shared secret
     * @param payload the content to cipher (must be 16 bytes long)
     */
    byte[] cipherWithSecret(byte[] secret, byte[] payload);

}