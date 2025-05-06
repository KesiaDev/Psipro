package com.example.apppisc.data.models

data class AppointmentSummary(
    val appointmentsCount: Int = 0,
    val activePatients: Int = 0,
    val completedAppointments: Int = 0,
    val cancelledAppointments: Int = 0
) 