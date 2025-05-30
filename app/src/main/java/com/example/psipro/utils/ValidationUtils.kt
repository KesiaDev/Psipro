package com.example.psipro.utils

import java.util.regex.Pattern
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.LocalTime
import java.time.Duration

object ValidationUtils {
    private val CPF_PATTERN = Pattern.compile("^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$")
    private val PHONE_PATTERN = Pattern.compile("^\\(?[1-9]{2}\\)? ?(?:[2-8]|9[1-9])[0-9]{3}-?[0-9]{4}$")
    private val EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)
    
    fun isValidCPF(cpf: String): Boolean {
        if (!CPF_PATTERN.matcher(cpf).matches()) return false
        
        val numbers = cpf.replace(Regex("[^0-9]"), "")
        if (numbers.length != 11) return false
        
        // Validação dos dígitos verificadores
        val digit1 = calculateCPFDigit(numbers.substring(0, 9))
        val digit2 = calculateCPFDigit(numbers.substring(0, 9) + digit1)
        
        return numbers.endsWith("$digit1$digit2")
    }
    
    private fun calculateCPFDigit(cpf: String): Int {
        var sum = 0
        var weight = cpf.length + 1
        
        for (digit in cpf) {
            sum += digit.toString().toInt() * weight
            weight--
        }
        
        val remainder = sum % 11
        return if (remainder < 2) 0 else 11 - remainder
    }
    
    fun isValidPhone(phone: String): Boolean {
        return PHONE_PATTERN.matcher(phone).matches()
    }
    
    fun isValidEmail(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches()
    }
    
    fun isValidBirthDate(birthDate: String, minAge: Int = 0, maxAge: Int = 120): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val date = LocalDate.parse(birthDate, formatter)
            val age = Period.between(date, LocalDate.now()).years
            age in minAge..maxAge
        } catch (e: Exception) {
            false
        }
    }
    
    fun isValidSessionValue(value: Double): Boolean {
        return value > 0
    }
    
    fun isValidAppointmentTime(startTime: String, endTime: String, minDuration: Int = 30, maxDuration: Int = 240): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val start = LocalDate.now().atTime(LocalTime.parse(startTime, formatter))
            val end = LocalDate.now().atTime(LocalTime.parse(endTime, formatter))
            
            val duration = Duration.between(start, end).toMinutes()
            duration in minDuration..maxDuration
        } catch (e: Exception) {
            false
        }
    }
    
    fun isValidWorkingHours(time: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val time = LocalTime.parse(time, formatter)
            time.isAfter(LocalTime.of(7, 0)) && time.isBefore(LocalTime.of(20, 0))
        } catch (e: Exception) {
            false
        }
    }
} 