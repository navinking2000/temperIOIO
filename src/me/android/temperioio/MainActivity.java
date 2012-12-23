package me.android.temperioio;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends IOIOActivity {

	private final int TMP_PIN = 34;
	
	TextView temperatureTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		temperatureTextView = (TextView) findViewById(R.id.temperature_textview);
	}

	class Looper extends BaseIOIOLooper {
		private AnalogInput mTempInput;
		
//		int THERMISTORPIN A0         
		// resistance at 25 degrees C
		int Ro = 10000;      
		// temp. for nominal resistance (almost always 25 C)
		int TEMPERATURENOMINAL = 25;   
		// how many samples to take and average, more takes longer
		// but is more 'smooth'
//		int NUMSAMPLES = 5;
		// The beta coefficient of the thermistor (usually 3000-4000)
		int B = 4100;
		// the value of the 'other' resistor
		int SERIESRESISTOR = 10000; 
		@Override
		protected void setup() throws ConnectionLostException
				{
			mTempInput = ioio_.openAnalogInput(TMP_PIN);
			
		}
		
		@Override
		public void loop() throws ConnectionLostException {
			try {

				float raw = mTempInput.read();
				
				float R = 10000/raw - 10000;// R1 = (R2*Vi/Vo) - R2; Voltage Divider
				
				float steinhart;
				steinhart = R / Ro; // (R/Ro)
				steinhart = (float) Math.log(steinhart); // ln(R/Ro)
				steinhart /= B; // 1/B * ln(R/Ro)
				steinhart += 1.0 / (TEMPERATURENOMINAL + 273.15); // + (1/ To)
				steinhart = (float) (1.0 /steinhart); // Invert
				steinhart -= 273.15; //convert to Cº | celsius = kelvin - 273.15

				String tempString = String.format("%.2f", steinhart) + "ºC";
				
				setText(tempString);
				
				Thread.sleep(1000);
				
			} catch (InterruptedException e) {
				ioio_.disconnect();
				setText(e.getMessage());
			} catch (ConnectionLostException e) {
				setText(e.getMessage());
				throw e;
			}
		}
		
	}
	
	@Override
	protected IOIOLooper createIOIOLooper() {
		// TODO Auto-generated method stub
		return new Looper();
	}
	
	private void setText(final String s) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				temperatureTextView.setText(s);		
			}
		});
		
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.activity_main, menu);
//		return true;
//	}

	
}
