package com.applozic.mobicommons.commons.core.utils;


import android.text.TextUtils;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * @author devashish
 */
public class ContactNumberUtils {

    public static String getPhoneNumber(String number, String defaultCountryCode) {
        if (TextUtils.isEmpty(number)) {
            return "";
        }

        PhoneNumber phoneNumber;
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        if (TextUtils.isEmpty(number)) {
            return "";
        }

        long contactNumber = 0;
        int countryCode = 0;

        try {
            phoneNumber = phoneUtil.parse(number, defaultCountryCode);
            if (phoneNumber.hasCountryCode()) {
                countryCode = phoneNumber.getCountryCode();
            }

            contactNumber = phoneNumber.getNationalNumber();
        } catch (Exception ex) {
            try {
                contactNumber = Long.parseLong(number);
            } catch (Exception e) {
                return number;
            }
        } finally {

        }
        return "+" + countryCode + contactNumber;
    }

    public static String getPhoneNumbers(String to, String defaultCountryCode) {
        String contactNumbers = "";
        String[] toNumbers = to.split(",");
        for (String toNumber : toNumbers) {
            if (!TextUtils.isEmpty(defaultCountryCode) && !TextUtils.isEmpty(toNumber)) {
                contactNumbers += ContactNumberUtils.getPhoneNumber(toNumber, defaultCountryCode) + ",";
            } else {
                contactNumbers += toNumber + ",";
            }
        }

        if (contactNumbers.endsWith(",")) {
            contactNumbers = contactNumbers.substring(0, contactNumbers.length() - 1);
        }
        return contactNumbers;
    }
}

