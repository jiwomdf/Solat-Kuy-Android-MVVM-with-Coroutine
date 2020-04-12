package com.programmergabut.solatkuy.broadcaster

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.programmergabut.solatkuy.data.model.entity.PrayerLocal
import com.programmergabut.solatkuy.ui.main.view.MainActivity
import com.programmergabut.solatkuy.util.EnumPrayer
import com.programmergabut.solatkuy.util.NotificationHelper
import java.lang.Exception
import java.util.*

/*
 * Created by Katili Jiwo Adi Wiyono on 27/03/20.
 */

class PrayerBroadcastReceiver: BroadcastReceiver() {

    private var pID: Int? = null
    private var pName: String? = null
    private var pTime: String? = null
    private var pCity: String? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val mNotificationHelper =
            NotificationHelper(context!!)

        pID = intent?.getIntExtra("prayer_id", -1)
        pName = intent?.getStringExtra("prayer_name")
        pTime = intent?.getStringExtra("prayer_time")
        pCity = intent?.getStringExtra("prayer_city")
        val listPrayerBundle = intent?.extras?.getBundle("list_prayer_bundle")

        if(pID == -1 || pName.isNullOrEmpty() || pTime.isNullOrEmpty() || listPrayerBundle == null)
            throw Exception("PrayerBroadcastReceiver")

        if(pCity == null)
            pCity = "-"

        val nextIntent = Intent(context, MainActivity::class.java)
        nextIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK


        when (pID!!) {
            EnumPrayer.nId1 -> {
                val pendingIntent = PendingIntent.getActivity(context, EnumPrayer.nId1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                val nb = mNotificationHelper.getPrayerReminderNC(EnumPrayer.nId1, pTime!!, pCity!!, pName!!, listPrayerBundle, pendingIntent)
                mNotificationHelper.getManager()?.notify(EnumPrayer.nId1, nb.build())
            }
            EnumPrayer.nId2 -> {
                val pendingIntent = PendingIntent.getActivity(context, EnumPrayer.nId2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                val nb = mNotificationHelper.getPrayerReminderNC(EnumPrayer.nId2, pTime!!, pCity!!, pName!!, listPrayerBundle, pendingIntent)
                mNotificationHelper.getManager()?.notify(EnumPrayer.nId2, nb.build())
            }
            else -> {
                val pendingIntent = PendingIntent.getActivity(context, EnumPrayer.nIdMain, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                val nb = mNotificationHelper.getPrayerReminderNC(pID!!, pTime!!, pCity!!, pName!!, listPrayerBundle, pendingIntent)
                mNotificationHelper.getManager()?.notify(EnumPrayer.nIdMain, nb.build())

                executeNextNotification(listPrayerBundle,context, pCity!!)
            }
        }

    }


    private fun executeNextNotification(listPrayerBundle: Bundle?, context: Context, pCity: String) {

        val listData = bundleDeserializer(listPrayerBundle)

        val intent = Intent(context, PrayerBroadcastReceiver::class.java)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val selID = selNextPrayer(listData, pID!!)
        //selID = PrayerLocal(4,"mantap 4",true,"14:10") #testing purpose

        selID?.let{

            val hour = it.prayerTime.split(":")[0].trim()
            val minute = it.prayerTime.split(":")[1].split(" ")[0].trim()

            val c = Calendar.getInstance()
            c.set(Calendar.HOUR_OF_DAY, hour.toInt())
            c.set(Calendar.MINUTE, minute.toInt())
            c.set(Calendar.SECOND, 0)

            intent.putExtra("prayer_id", it.prayerID)
            intent.putExtra("prayer_name", it.prayerName)
            intent.putExtra("prayer_time", it.prayerTime)
            intent.putExtra("prayer_city", pCity)
            intent.putExtra("list_prayer_bundle", listPrayerBundle)

            val pendingIntent = PendingIntent.getBroadcast(context, it.prayerID, intent, 0)

            if(c.before(Calendar.getInstance()))
                c.add(Calendar.DATE, 1)

            if(it.isNotified)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.timeInMillis, pendingIntent)
            else
                alarmManager.cancel(pendingIntent)
        }

    }

    private fun bundleDeserializer(listPrayerBundle: Bundle?): MutableList<PrayerLocal> {

        val listData = mutableListOf<PrayerLocal>()

        val listPID  =  listPrayerBundle?.getIntegerArrayList("list_PID")
        val listPName = listPrayerBundle?.getStringArrayList("list_PName")
        val listPTime = listPrayerBundle?.getStringArrayList("list_PTime")
        val listPIsNotified = listPrayerBundle?.getIntegerArrayList("list_PIsNotified")
        val listPCity = listPrayerBundle?.getStringArrayList("list_PCity")

        val listCount = listPID?.count()

        for(i in 0 until listCount!!){

            val isNotified: Boolean = listPIsNotified!![i] == 1

            listData.add(PrayerLocal(listPID[i], listPName?.get(i)!!, isNotified, listPTime?.get(i)!!))
        }

        return listData
    }

    private fun selNextPrayer(listData: MutableList<PrayerLocal>, selID: Int): PrayerLocal? {

        listData.sortBy { x -> x.prayerID }

        val firstID = listData[0].prayerID
        val lastID = listData[listData.count() - 1].prayerID

        var nextID = selID + 1

        if(nextID > lastID)
            nextID = firstID

        return listData.find { x -> x.prayerID == nextID }
    }

}