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
package m3da.codec.impl;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import m3da.codec.EcdhService;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Charsets;

/**
 * Unit test for {@link SecurityUtils} ECDH methods
 */
public class EcdhTests {

    EcdhService utils = new EcdhServiceImpl();

    @Test
    public void generate_two_key_pair_and_share_a_secret() {
        KeyPair a = utils.generateEcdhKeyPair();
        KeyPair b = utils.generateEcdhKeyPair();

        byte[] aPubCert = utils.getPublicKeyCertificate(a);
        byte[] bPubCert = utils.getPublicKeyCertificate(b);

        byte[] aSecret = utils.computeSharedSecret(a, bPubCert);

        byte[] bSecret = utils.computeSharedSecret(b, aPubCert);

        Assert.assertTrue(Arrays.equals(aSecret, bSecret));
    }

    @Test
    public void exchange_new_password_using_ecdh() {
        String newPassword = "$$$the New P4ssw0rd!!!!";

        KeyPair serverKeys = utils.generateEcdhKeyPair();
        KeyPair clientKeys = utils.generateEcdhKeyPair();

        byte[] clientPubCert = utils.getPublicKeyCertificate(clientKeys);
        byte[] theSecret = utils.computeSharedSecret(serverKeys, clientPubCert);
        byte[] message = utils.cipherWithSecret(theSecret, md5(newPassword.getBytes()));

        byte[] serverPubCert = utils.getPublicKeyCertificate(serverKeys);
        byte[] clientSecret = utils.computeSharedSecret(clientKeys, serverPubCert);

        byte[] key = md5(clientSecret);

        for (int i = 0; i < 16; i++) {
            message[i] ^= key[i];
        }
        Assert.assertTrue(Arrays.equals(md5(newPassword.getBytes(Charsets.UTF_8)), message));
    }

    private byte[] md5(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            return digest.digest(data);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("no MD5 provider in the JVM");
        }
    }

}
