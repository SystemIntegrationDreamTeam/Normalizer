/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.normalizer;

/**
 *
 * @author Buhrkall
 */
public class JResponse {

    String ssn;
    String interestRate;
    String bankName;

    public JResponse(String ssn, String interestRate) {
        this.ssn = ssn;
        this.interestRate = interestRate;
    }

    public void setBank(String bankName){
    this.bankName = bankName;
    }
}
