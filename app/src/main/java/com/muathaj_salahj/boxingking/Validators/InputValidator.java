package com.muathaj_salahj.boxingking.Validators;

import android.text.TextUtils;
import android.util.Patterns;

// This class contains static functions for inputs validation
public abstract class InputValidator {

    // validating the inputs for the signup method (SignupActivity) [name, email, password]
    // return null if the inputs are valid else return error message string
    public static String isValidSignupInputs(String name, String email, String password){

        String result = "";

        if(!isValidName(name)){

            result = result.concat("Invalid Name (Min Name Length Is 3 Characters) ");
        }

        if(!isValidEmail(email)){
            result = result.concat("Invalid Email ");
        }

        if(!isValidPassword(password)){
            result = result.concat("Invalid Password (Min Password Length Is 6 Characters) ");
        }

        return  result.equals("") ? null : result;
    }

    // validating the inputs for the login method (LoginActivity) [email, password]
    // return null if the inputs are valid else return error message string
    public static String isValidLoginInputs(String email, String password){

        String result = "";

        if(!isValidEmail(email)){
            result = result.concat("Invalid Email ");
        }

        if(!isValidPassword(password)){
            result = result.concat("Invalid Password (Min Password Length Is 6 Chars) ");
        }

        return  result.equals("") ? null : result;
    }

    // validating the inputs for the following method (FollowsActivity) [email]
    // return null if the inputs are valid else return error message string
    public static String isValidFollowInputs(String email){

        String result = "";

        if(!isValidEmail(email)){
            result = result.concat("Invalid Email ");
        }

        return  result.equals("") ? null : result;
    }

    // validating the email format [true/false]
    public static boolean isValidEmail(String email) {

        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    // validating the password [true/false]
    public static boolean isValidPassword(String password){

        int minPasswordLength = 6;

        return password.trim().length() >= minPasswordLength;
    }

    // validating the name [true/false]
    public static boolean isValidName(String name){

        int minNameLength = 3;

        return name.trim().length() >= minNameLength;
    }
}
