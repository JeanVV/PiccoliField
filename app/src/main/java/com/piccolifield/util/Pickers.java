package com.piccolifield.util;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Pickers {
    private static final String CERO = "0";
    private static final String DOS_PUNTOS = ":";
    private static final String BARRA = "/";

    //Calendario para obtener fecha & hora
    public static final Calendar c = Calendar.getInstance();

    //Fecha
    static final int mes = c.get(Calendar.MONTH);
    static final int dia = c.get(Calendar.DAY_OF_MONTH);
    static final int anio = c.get(Calendar.YEAR);

    //Hora
    static final int hora = c.get(Calendar.HOUR_OF_DAY);
    static final int minuto = c.get(Calendar.MINUTE);


    public static void obtenerFecha(final Context context, final EditText etFecha, String flag){
        final DatePickerDialog recogerFecha = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(final DatePicker view, final int year, final int month, final int dayOfMonth) {

                final int mesActual = month + 1;

                final String diaFormateado = (dayOfMonth < 10)? CERO + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
                final String mesFormateado = (mesActual < 10)? CERO + String.valueOf(mesActual):String.valueOf(mesActual);

                etFecha.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);


            }
        },anio, mes, dia);

        //flag: "anterior", "posterior"
        if (flag.equals("anterior")){
            //Restringir las fechas posteriores a la actual
            recogerFecha.getDatePicker().setMaxDate(System.currentTimeMillis());
        }else{
            //Restringir las fechas anteriores a la actual
            recogerFecha.getDatePicker().setMinDate(System.currentTimeMillis());
        }

        recogerFecha.show();

    }

    public static void obtenerFecha(final Context context, final Chip chipFecha, String flag){
        final DatePickerDialog recogerFecha = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(final DatePicker view, final int year, final int month, final int dayOfMonth) {

                final int mesActual = month + 1;

                final String diaFormateado = (dayOfMonth < 10)? CERO + String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
                final String mesFormateado = (mesActual < 10)? CERO + String.valueOf(mesActual):String.valueOf(mesActual);

                chipFecha.setText(diaFormateado + BARRA + mesFormateado + BARRA + year);


            }
        },anio, mes, dia);

        //flag: "anterior", "posterior"
        if (flag.equals("anterior")){
            //Restringir las fechas posteriores a la actual
            recogerFecha.getDatePicker().setMaxDate(System.currentTimeMillis());
        }else{
            //Restringir las fechas anteriores a la actual
            recogerFecha.getDatePicker().setMinDate(System.currentTimeMillis());
        }


        recogerFecha.show();

    }

    public static void obtenerHora(final Context context, final EditText etHora){
        final TimePickerDialog recogerHora = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {

                final String horaFormateada =  (hourOfDay < 9)? String.valueOf(CERO + hourOfDay) : String.valueOf(hourOfDay);
                final String minutoFormateado = (minute < 9)? String.valueOf(CERO + minute):String.valueOf(minute);

                final String AM_PM;
                if(hourOfDay < 12) {
                    AM_PM = "a.m.";
                } else {
                    AM_PM = "p.m.";
                }

                etHora.setText(horaFormateada + DOS_PUNTOS + minutoFormateado + " " + AM_PM);
            }

        }, hora, minuto, false);

        recogerHora.show();
    }

    public static void obtenerHora(final Context context, final Chip chipHora){
        final TimePickerDialog recogerHora = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {

                final String horaFormateada =  (hourOfDay < 9)? String.valueOf(CERO + hourOfDay) : String.valueOf(hourOfDay);
                final String minutoFormateado = (minute < 9)? String.valueOf(CERO + minute):String.valueOf(minute);

                final String AM_PM;
                if(hourOfDay < 12) {
                    AM_PM = "a.m.";
                } else {
                    AM_PM = "p.m.";
                }

                chipHora.setText(horaFormateada + DOS_PUNTOS + minutoFormateado + " " + AM_PM);
            }

        }, hora, minuto, false);

        recogerHora.show();
    }

    public static void obtenerHora24(final Context context, final EditText etHora) {
        // Obtener la hora actual para inicializar el diálogo
        final Calendar calendarioActual = Calendar.getInstance();
        int hora = calendarioActual.get(Calendar.HOUR_OF_DAY);
        int minuto = calendarioActual.get(Calendar.MINUTE);
        int segundo = calendarioActual.get(Calendar.SECOND); // Aunque TimePicker no da segundos, los añadiremos al Calendar para el formato

        final TimePickerDialog recogerHora = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {

                // Creamos un objeto Calendar para establecer la hora seleccionada
                final Calendar calendarioSeleccionado = Calendar.getInstance();
                calendarioSeleccionado.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendarioSeleccionado.set(Calendar.MINUTE, minute);
                // Mantenemos los segundos originales (o podrías fijarlos a 0)
                // Para el propósito de HH:mm:ss, es mejor fijarlos a 00 ya que TimePicker no los selecciona.
                calendarioSeleccionado.set(Calendar.SECOND, 0);

                // Formato de 24 horas (HH:mm:ss)
                // 'HH' es hora en formato 24 horas (00-23)
                final SimpleDateFormat formato24Horas = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

                // Aplicamos el formato a la hora seleccionada
                final String horaFormateada = formato24Horas.format(calendarioSeleccionado.getTime());

                // Establecemos el resultado en el EditText
                etHora.setText(horaFormateada);
            }

            // El último argumento 'true' fuerza el uso del formato de 24 horas en el diálogo TimePicker
        }, hora, minuto, true);

        recogerHora.show();
    }
}
