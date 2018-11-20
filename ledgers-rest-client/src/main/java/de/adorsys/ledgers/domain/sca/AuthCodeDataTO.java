/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.domain.sca;

import java.util.Objects;

public class AuthCodeDataTO {
    private String userLogin;
    private String scaUserDataId;
    private String paymentId;
    private String opData;

    public AuthCodeDataTO() {
    }

    public AuthCodeDataTO(String userLogin, String scaUserDataId, String paymentId, String opData) {
        this.userLogin = userLogin;
        this.scaUserDataId = scaUserDataId;
        this.paymentId = paymentId;
        this.opData = opData;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getScaUserDataId() {
        return scaUserDataId;
    }

    public void setScaUserDataId(String scaUserDataId) {
        this.scaUserDataId = scaUserDataId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOpData() {
        return opData;
    }

    public void setOpData(String opData) {
        this.opData = opData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        AuthCodeDataTO that = (AuthCodeDataTO) o;
        return Objects.equals(userLogin, that.userLogin) &&
                       Objects.equals(scaUserDataId, that.scaUserDataId) &&
                       Objects.equals(paymentId, that.paymentId) &&
                       Objects.equals(opData, that.opData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userLogin, scaUserDataId, paymentId, opData);
    }

    @Override
    public String toString() {
        return "AuthCodeDataTO{" +
                       "userLogin='" + userLogin + '\'' +
                       ", scaUserDataId='" + scaUserDataId + '\'' +
                       ", paymentId='" + paymentId + '\'' +
                       ", opData='" + opData + '\'' +
                       '}';
    }
}
