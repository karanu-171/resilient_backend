/*
 * Copyright (c) 2022. Omukubwa Software Solutions (OSS)
 */

package com.emtech.loanapp.Auth.Utilities;

import java.util.Random;

public class ToolKit {

    //Generate random java alphanumeric string
    public String generatePassword() {
        String characters = "01234ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz56789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        while (sb.length() < 8) {
            int index = (int) (rnd.nextFloat() * characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    //Generate Password reset token
    public int generatePassResetToken() {
        Random rn = new Random();
        return rn.nextInt(1000000 - 110 + 1) + 2;
    }
}
