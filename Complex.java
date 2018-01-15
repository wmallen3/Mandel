// Complex numbers.
// Bill Allen
// Copyright (c) 2018 by William Allen.  All rights reserved.
//
// Modification History
//      January 3, 2018     BA      Initial Draft


import static java.lang.Math.sqrt;
import static java.lang.Math.pow;
import static java.lang.Math.exp;
import static java.lang.Math.atan2;
import static java.lang.Math.sin;
import static java.lang.Math.cos;

class Complex {
    private double  re = 0.0;
    private double  im = 0.0;
    
    
    public double real() {
        return re;
    }
    
    public double imag() {
        return im;
    }
    
    public String toString() {
        String r = "" + re;
        String i = "" + im;
        
        if (r.equals("-0.0")) r = "0.0";
        if (i.equals("-0.0")) i = "0.0";
        
        return r + " + (" + i + ")i";
    }
    
    public Complex() {
        re = 0.0;
        im = 0.0;
    }
    
    public Complex(double real) {
        re = real;
        im = 0.0;
    }
    
    public Complex(float real) {
        re = (double) real;
        im = 0.0;
    }
    
    public Complex(int real) {
        re = (double) real;
        im = 0.0;
    }
    
    public Complex(short real) {
        re = (double) real;
        im = 0.0;
    }
    
    public Complex(long real) {
        re = (double) real;
        im = 0.0;
    }
    
    public Complex(double real, double imaginary) {
        re = real;
        im = imaginary;
    }
    
    public Complex(Complex c) {
        re = c.re;
        im = c.im;
    }
    
    
    public Complex plus(byte arg) {
        return new Complex(re + (double) arg, im);
    }
    
    public Complex plus(short arg) {
        return new Complex(re + (double) arg, im);
    }
    
    public Complex plus(int arg) {
        return new Complex(re + (double) arg, im);
    }
    
    public Complex plus(long arg) {
        return new Complex(re + (double) arg, im);
    }
    
    public Complex plus(float arg) {
        return new Complex(re + (double) arg, im);
    }
    
    public Complex plus(double arg) {
        return new Complex(re + arg, im);
    }
    
    public Complex plus(Complex arg) {
        return new Complex(re + arg.re, im + arg.im);
    }
    
    public Complex minus(byte arg) {
        return new Complex(re - (double) arg, im);
    }
    
    public Complex minus(short arg) {
        return new Complex(re - (double) arg, im);
    }
    
    public Complex minus(int arg) {
        return new Complex(re - (double) arg, im);
    }
    
    public Complex minus(long arg) {
        return new Complex(re - (double) arg, im);
    }
    
    public Complex minus(float arg) {
        return new Complex(re - (double) arg, im);
    }
    
    public Complex minus(double arg) {
        return new Complex(re - arg, im);
    }
    
    public Complex minus(Complex arg) {
        return new Complex(re - arg.re, im - arg.im);
    }
    
    public Complex times(byte arg) {
        return new Complex(re * ((double) arg), im * ((double) arg)); 
    }
    
    public Complex times(short arg) {
        return new Complex(re * ((double) arg), im * ((double) arg)); 
    }
    
    public Complex times(int arg) {
        return new Complex(re * ((double) arg), im * ((double) arg)); 
    }
    
    public Complex times(long arg) {
        return new Complex(re * ((double) arg), im * ((double) arg)); 
    }
    
    public Complex times(float arg) {
        return new Complex(re * ((double) arg), im * ((double) arg)); 
    }
    
    public Complex times(double arg) {
        return new Complex(re * arg, im * arg); 
    }
    
    public Complex times(Complex arg) {
        return new Complex(re * arg.re - im * arg.im, re * arg.im + im * arg.re); 
    }
    
    public Complex conjugate() {
        return new Complex(re, -im);
    }
    
    public double norm2() {
        return (re * re + im * im);
    }
    
    public double norm() {
        return sqrt(norm2());
    }
    
    public Complex inverse() throws ArithmeticException {
        Complex conj = conjugate();
        double  denom = 0.0;
        
        denom = 1.0 / (re * re + im * im);
                
        return conj.times(denom);
    }
    
    public double arg() {
        return atan2(im, re);
    }
    
    public Complex exp() throws ArithmeticException {
        double  argument = arg();
        double  modulus = norm();
        
        return (new Complex(cos(argument), sin(argument)).times(Math.exp(norm())));
    }
}



