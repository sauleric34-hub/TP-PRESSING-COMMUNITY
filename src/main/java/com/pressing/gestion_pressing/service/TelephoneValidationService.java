package com.pressing.gestion_pressing.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.stereotype.Service;

@Service
public class TelephoneValidationService {

    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    /**
     * Valide de manière très stricte si un numéro est un vrai numéro du Cameroun (CM).
     */
    public boolean estValideCameroun(String telephone) {
        try {
            // Analyse du numéro
            Phonenumber.PhoneNumber numero = phoneNumberUtil.parse(telephone, "CM");
            
            // 1. Vérification de la région (Cameroun)
            if (!"CM".equals(phoneNumberUtil.getRegionCodeForNumber(numero))) {
                return false;
            }

            // 2. Vérification stricte du plan de numérotation de l'ART (Cameroun)
            // isValidNumberForRegion est plus précis que isValidNumber
            if (!phoneNumberUtil.isValidNumberForRegion(numero, "CM")) {
                return false;
            }

            // 3. Vérification du type (Mobile ou Fixe uniquement)
            PhoneNumberUtil.PhoneNumberType type = phoneNumberUtil.getNumberType(numero);
            return type == PhoneNumberUtil.PhoneNumberType.MOBILE || 
                   type == PhoneNumberUtil.PhoneNumberType.FIXED_LINE;
                   
        } catch (NumberParseException e) {
            return false;
        }
    }

    public String formaterInternational(String telephone) {
        try {
            Phonenumber.PhoneNumber numero = phoneNumberUtil.parse(telephone, "CM");
            return phoneNumberUtil.format(numero, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            return telephone;
        }
    }
}
