/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.user.lib.model;

import static org.apache.james.user.lib.model.Algorithm.HashingMode.LEGACY;
import static org.apache.james.user.lib.model.Algorithm.HashingMode.PLAIN;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.james.core.Username;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DefaultUserTest {

    private DefaultUser user;

    @Before
    public void setUp() {
        user = new DefaultUser(
                Username.of("joe"),
                "5en6G6MezRroT3XKqkdPOmY/", // SHA-1 legacy hash of "secret"
                Algorithm.of("SHA-1", LEGACY),
                Algorithm.of("SHA-256", PLAIN));
    }

    @Test
    public void shouldYieldVerifyAlgorithm() {
        assertThat(user.getHashAlgorithm().asString()).isEqualTo("SHA-1/legacy");
        assertThat(user.getHashAlgorithm().getHashingMode()).isEqualToIgnoringCase(LEGACY.name());
    }

    @Test
    public void shouldVerifyPasswordUsingVerifyAlgorithm() {
        assertThat(user.verifyPassword("secret")).isTrue();
        assertThat(user.verifyPassword("secret2")).isFalse();
    }

    @Test
    public void shouldSetPasswordUsingUpdateAlgorithm() {
        user.setPassword("secret2");
        assertThat(user.getHashAlgorithm().asString()).isEqualTo("SHA-256/plain");
        assertThat(user.getHashAlgorithm().getHashingMode()).isEqualToIgnoringCase(PLAIN.name());

        assertThat(user.verifyPassword("secret2")).isTrue();
        assertThat(user.verifyPassword("secret")).isFalse();
    }

    private static Stream<Arguments> sha1LegacyTestBed() {
        return Stream.of(
            Arguments.of("myPassword", "VBPuJHI7uixaa6LQGWx4s+5G"),
            Arguments.of("otherPassword", "ks40t+AjBnHsMaC1Is/6+mtb"),
            Arguments.of("", "2jmj7l5rSw0yVb/vlWAYkK/Y"),
            Arguments.of("a", "hvfkN/qlp/zhXR3cuerq6jd2"));
    }

    @ParameterizedTest
    @MethodSource("sha1LegacyTestBed")
    void testSha1Legacy(String password, String expectedHash) throws Exception {
        assertThat(DefaultUser.digestString(Optional.ofNullable(password).orElse(""),
            Algorithm.of("SHA-1", "legacy"), "salt"))
            .isEqualTo(expectedHash);
    }

    private static Stream<Arguments> sha512LegacyTestBed() {
        return Stream.of(
            Arguments.of("myPassword", "RQrQPbk5XfzLXgMGb9fxbPuith4j1RY3NxRHFFkFLskKmkvzoVHmAOqKrtNuO4who9OKsXBYOXSd\r\nEw2kOA8U"),
            Arguments.of("otherPassword", "6S2kG/b6oHgWBXQjKDKTayXWu2cs9374lxFrL9uVpmYUlq0lw/ZFU9svMtYVDV5aVjJqRbLWZ/df\r\neaaJwYxk"),
            Arguments.of("", "z4PhNX7vuL3xVChQ1m2AB9Yg5AULVxXcg/SpIdNs6c5H0NE8XYXysP+DGNKHfuwvY7kxvUdBeoGl\r\nODJ6+Sfa"),
            Arguments.of("a", "H0D8ktokFpR1CXnubPWC8tXX0o4YM13gWrxU0FYOD1MChgxlK/CNVgJSql50IQVG82n7u86MEs/H\r\nlXsmUv6a"));
    }

    @ParameterizedTest
    @MethodSource("sha512LegacyTestBed")
    void testSha512Legacy(String password, String expectedHash) throws Exception {
        assertThat(DefaultUser.digestString(password, Algorithm.of("SHA-512", "legacy"), "salt"))
            .isEqualTo(expectedHash);
    }

    private static Stream<Arguments> sha1TestBed() {
        return Stream.of(
            Arguments.of("myPassword", "VBPuJHI7uixaa6LQGWx4s+5GKNE=\r\n"),
            Arguments.of("otherPassword", "ks40t+AjBnHsMaC1Is/6+mtb05s=\r\n"),
            Arguments.of("", "2jmj7l5rSw0yVb/vlWAYkK/YBwk=\r\n"),
            Arguments.of("a", "hvfkN/qlp/zhXR3cuerq6jd2Z7g=\r\n"));
    }

    @ParameterizedTest
    @MethodSource("sha1TestBed")
    void testSha1(String password, String expectedHash) throws Exception {
        assertThat(DefaultUser.digestString(password, Algorithm.of("SHA-1"), "salt"))
            .isEqualTo(expectedHash);
    }

    private static Stream<Arguments> sha512TestBed() {
        return Stream.of(
            Arguments.of("myPassword", "RQrQPbk5XfzLXgMGb9fxbPuith4j1RY3NxRHFFkFLskKmkvzoVHmAOqKrtNuO4who9OKsXBYOXSd\r\nEw2kOA8USA==\r\n"),
            Arguments.of("otherPassword", "6S2kG/b6oHgWBXQjKDKTayXWu2cs9374lxFrL9uVpmYUlq0lw/ZFU9svMtYVDV5aVjJqRbLWZ/df\r\neaaJwYxkhQ==\r\n"),
            Arguments.of("", "z4PhNX7vuL3xVChQ1m2AB9Yg5AULVxXcg/SpIdNs6c5H0NE8XYXysP+DGNKHfuwvY7kxvUdBeoGl\r\nODJ6+SfaPg==\r\n"),
            Arguments.of("a", "H0D8ktokFpR1CXnubPWC8tXX0o4YM13gWrxU0FYOD1MChgxlK/CNVgJSql50IQVG82n7u86MEs/H\r\nlXsmUv6adQ==\r\n"));
    }

    @ParameterizedTest
    @MethodSource("sha512TestBed")
    void testSha512(String password, String expectedHash) throws Exception {
        assertThat(DefaultUser.digestString(password, Algorithm.of("SHA-512"), "salt"))
            .isEqualTo(expectedHash);
    }

    private static Stream<Arguments> PBKDF2TestBed() {
        return Stream.of(
            Arguments.of("myPassword", "Ag2g49zxor0w11yguLUMQ7EKBokU81LkvLyDqubWtQq7R5V21HVqZ+CEjEQxBLGfi35RFyesJtxb\r\n" +
                "L5/VRCpI3g==\r\n"),
            Arguments.of("otherPassword", "4KFfGIjbZqhaqZfr1rKWcoY5vkeps3/+x5BwU342kUbGGoW30kaP98R5iY6SNGg0yOaPBcB8EWqJ\r\n" +
                "96RtIMnIYQ==\r\n"),
            Arguments.of("", "6grdNX1hpxA5wJPXhBUJhz4qUoUSRZE0F3rqoPR+PYedDklDomJ0LPRV5f1SMNAX0fRgmQ8WDe6k\r\n" +
                "2qr1Nc/orA==\r\n"),
            Arguments.of("a", "WxpwqV5V9L3QR8xi8D8INuH0UH5oLeq+ZuXb6J1bAfhHp3urVOtAr+bwksC3JQRyC7QHE9MLfn61\r\n" +
                "nTXo5johrQ==\r\n"));
    }

    @ParameterizedTest
    @MethodSource("PBKDF2TestBed")
    void testPBKDF2(String password, String expectedHash) {
        assertThat(DefaultUser.digestString(password, Algorithm.of("PBKDF2"), "salt"))
            .isEqualTo(expectedHash);
    }

    private static Stream<Arguments> PBKDF210IterationTestBed() {
        return Stream.of(
            Arguments.of("myPassword", "AoNuFZ7ZI6vHU8obYASuuLPaQcr8fGentKWsOawBNTIc7MNJMbo4yNjo0pcCVK6J/XAfbISEKugt\r\n" +
                "HwDeSUA10A==\r\n"),
            Arguments.of("otherPassword", "e4+swbwo1s3X665INoRVsENXrgJtC7SMws9G3Y0GBoLZBkqZQzE2aT2WLd+hOlf3s/wwQe10MA0Q\r\n" +
                "xMJQIcIosQ==\r\n"),
            Arguments.of("", "ZBXj9rrLc4L9hHXOBPpDd5ot9DDB6qaq1g2mbAMOivpZe3eYw1ehdFXbU9pwpI4y/+MZlLkG3E1S\r\n" +
                "WRQXuUZqag==\r\n"),
            Arguments.of("a", "i1iWZzuaqsFotT998+stRqyrcyUrZ0diBJf9RJ52mUo0a074ykh8joWdrxhEsyd2Fh2DNO38TWxC\r\n" +
                "KkIK6taLxA==\r\n"));
    }

    @ParameterizedTest
    @MethodSource("PBKDF210IterationTestBed")
    void testPBKDF210Iteration(String password, String expectedHash) {
        assertThat(DefaultUser.digestString(password, Algorithm.of("PBKDF2-10"), "salt"))
            .isEqualTo(expectedHash);
    }

    private static Stream<Arguments> PBKDF210Iteration128KeySizeTestBed() {
        return Stream.of(
            Arguments.of("myPassword", "AoNuFZ7ZI6vHU8obYASuuA==\r\n"),
            Arguments.of("otherPassword", "e4+swbwo1s3X665INoRVsA==\r\n"),
            Arguments.of("", "ZBXj9rrLc4L9hHXOBPpDdw==\r\n"),
            Arguments.of("a", "i1iWZzuaqsFotT998+stRg==\r\n"));
    }

    @ParameterizedTest
    @MethodSource("PBKDF210Iteration128KeySizeTestBed")
    void testPBKDF210Iteration128KeySize(String password, String expectedHash) {
        assertThat(DefaultUser.digestString(password, Algorithm.of("PBKDF2-10-128"), "salt"))
            .isEqualTo(expectedHash);
    }
}
