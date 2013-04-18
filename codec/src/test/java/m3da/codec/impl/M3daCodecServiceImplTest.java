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

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import m3da.codec.Hex;
import m3da.codec.M3daCodecService;
import m3da.codec.M3daCodecService.CipherMode;
import m3da.codec.dto.CipherAlgorithm;
import m3da.codec.dto.HmacType;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link M3daCodecServiceImpl}
 */
public class M3daCodecServiceImplTest {

    @InjectMocks
    private M3daCodecService service = new M3daCodecServiceImpl();

    @Mock
    private SecurityUtils securityUtils;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void hmac() throws Exception {

        service.hmac(HmacType.HMAC_SHA1, "myUsername".getBytes(), "myPassword".getBytes(),
                Hex.decodeHex("1234567890ABCDEF"), "the message body".getBytes());

        ArgumentCaptor<byte[]> kCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<byte[]> mCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(securityUtils).hmac(eq(HmacType.HMAC_SHA1.getDigest()), kCaptor.capture(), mCaptor.capture());

        // K = HMD5(username | HMD5(password))
        assertEquals("4234f2270b88d6ed6a8aabd64fbdac6c", Hex.encodeHexString(kCaptor.getValue()));

        // m = protectedEnveloppe | nonce
        assertEquals("746865206d65737361676520626f64791234567890abcdef", Hex.encodeHexString(mCaptor.getValue()));
    }

    @Test
    public void cipher_encryption_128bit_key() throws Exception {

        byte[] hmacMd5 = md5("anyContent".getBytes());
        when(securityUtils.hmac(eq("MD5"), Mockito.any(byte[].class), Mockito.any(byte[].class))).thenReturn(hmacMd5);

        ByteArrayInputStream is = new ByteArrayInputStream("input".getBytes());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        service.cipher(CipherMode.ENCRYPTION, CipherAlgorithm.AES_CTR_128, "password".getBytes(),
                Hex.decodeHex("1234567890ABCDEF"), is, os);

        // verify
        ArgumentCaptor<byte[]> kCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<byte[]> mCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(securityUtils).hmac(eq("MD5"), kCaptor.capture(), mCaptor.capture());

        // k = md5(password)
        assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", Hex.encodeHexString(kCaptor.getValue()));

        // m = nonce
        assertEquals("1234567890abcdef", Hex.encodeHexString(mCaptor.getValue()));

        ArgumentCaptor<byte[]> keyCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<byte[]> ivCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(securityUtils).cipher(eq(true), eq("AES"), eq("AES/CTR/NoPadding"), keyCaptor.capture(),
                ivCaptor.capture(), eq(is), eq(os));

        // key = hmacMd5 ( keyL | keyR, nonce)
        assertTrue(Arrays.equals(hmacMd5, keyCaptor.getValue()));

        // iv = HMD5(nonce)
        assertEquals("799d4f0bec27aff89de30b35e3ca4332", Hex.encodeHexString(ivCaptor.getValue()));
    }

    @Test
    public void cipher_encryption_256bit_key() throws Exception {

        byte[] hmacMd5 = md5("anyContent".getBytes());
        when(securityUtils.hmac(eq("MD5"), Mockito.any(byte[].class), Mockito.any(byte[].class))).thenReturn(hmacMd5);

        ByteArrayInputStream is = new ByteArrayInputStream("input".getBytes());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        service.cipher(CipherMode.ENCRYPTION, CipherAlgorithm.AES_CTR_256, "password".getBytes(),
                Hex.decodeHex("1234567890ABCDEF"), is, os);

        // verify

        ArgumentCaptor<byte[]> kCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<byte[]> mCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(securityUtils, times(2)).hmac(eq("MD5"), kCaptor.capture(), mCaptor.capture());

        // k = md5(password)
        assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", Hex.encodeHexString(kCaptor.getAllValues().get(0)));
        assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", Hex.encodeHexString(kCaptor.getAllValues().get(1)));

        // m (first call) = nonce
        assertEquals("1234567890abcdef", Hex.encodeHexString(mCaptor.getAllValues().get(0)));
        // m (second call) = nonce | nonce
        assertEquals("1234567890abcdef1234567890abcdef", Hex.encodeHexString(mCaptor.getAllValues().get(1)));

        ArgumentCaptor<byte[]> keyCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<byte[]> ivCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(securityUtils).cipher(eq(true), eq("AES"), eq("AES/CTR/NoPadding"), keyCaptor.capture(),
                ivCaptor.capture(), eq(is), eq(os));

        // key = hmacMd5 ( keyL | keyR, nonce)
        assertTrue(Arrays.equals(ArrayUtils.addAll(hmacMd5, hmacMd5), keyCaptor.getValue()));

        // iv = HMD5(nonce)
        assertEquals("799d4f0bec27aff89de30b35e3ca4332", Hex.encodeHexString(ivCaptor.getValue()));
    }

    @Test
    public void cipher_decryption_128bit_key() throws Exception {

        byte[] hmacMd5 = md5("anyContent".getBytes());
        when(securityUtils.hmac(eq("MD5"), Mockito.any(byte[].class), Mockito.any(byte[].class))).thenReturn(hmacMd5);

        ByteArrayInputStream is = new ByteArrayInputStream("input".getBytes());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        service.cipher(CipherMode.DECRYPTION, CipherAlgorithm.AES_CTR_128, "password".getBytes(),
                Hex.decodeHex("1234567890ABCDEF"), is, os);

        // verify
        ArgumentCaptor<byte[]> kCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<byte[]> mCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(securityUtils).hmac(eq("MD5"), kCaptor.capture(), mCaptor.capture());

        // k = md5(password)
        assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", Hex.encodeHexString(kCaptor.getValue()));

        // m = nonce
        assertEquals("1234567890abcdef", Hex.encodeHexString(mCaptor.getValue()));

        ArgumentCaptor<byte[]> keyCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<byte[]> ivCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(securityUtils).cipher(eq(false), eq("AES"), eq("AES/CTR/NoPadding"), keyCaptor.capture(),
                ivCaptor.capture(), eq(is), eq(os));

        // key = hmacMd5 ( keyL | keyR, nonce)
        assertTrue(Arrays.equals(hmacMd5, keyCaptor.getValue()));

        // iv = HMD5(nonce)
        assertEquals("799d4f0bec27aff89de30b35e3ca4332", Hex.encodeHexString(ivCaptor.getValue()));
    }

    @Test
    public void cipher_decryption_256bit_key() throws Exception {

        byte[] hmacMd5 = md5("anyContent".getBytes());
        when(securityUtils.hmac(eq("MD5"), Mockito.any(byte[].class), Mockito.any(byte[].class))).thenReturn(hmacMd5);

        ByteArrayInputStream is = new ByteArrayInputStream("input".getBytes());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        service.cipher(CipherMode.DECRYPTION, CipherAlgorithm.AES_CTR_256, "password".getBytes(),
                Hex.decodeHex("1234567890ABCDEF"), is, os);

        // verify

        ArgumentCaptor<byte[]> kCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<byte[]> mCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(securityUtils, times(2)).hmac(eq("MD5"), kCaptor.capture(), mCaptor.capture());

        // k = md5(password)
        assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", Hex.encodeHexString(kCaptor.getAllValues().get(0)));
        assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", Hex.encodeHexString(kCaptor.getAllValues().get(1)));

        // m (first call) = nonce
        assertEquals("1234567890abcdef", Hex.encodeHexString(mCaptor.getAllValues().get(0)));
        // m (second call) = nonce | nonce
        assertEquals("1234567890abcdef1234567890abcdef", Hex.encodeHexString(mCaptor.getAllValues().get(1)));

        ArgumentCaptor<byte[]> keyCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<byte[]> ivCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(securityUtils).cipher(eq(false), eq("AES"), eq("AES/CTR/NoPadding"), keyCaptor.capture(),
                ivCaptor.capture(), eq(is), eq(os));

        // key = hmacMd5 ( keyL | keyR, nonce)
        assertTrue(Arrays.equals(ArrayUtils.addAll(hmacMd5, hmacMd5), keyCaptor.getValue()));

        // iv = HMD5(nonce)
        assertEquals("799d4f0bec27aff89de30b35e3ca4332", Hex.encodeHexString(ivCaptor.getValue()));
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