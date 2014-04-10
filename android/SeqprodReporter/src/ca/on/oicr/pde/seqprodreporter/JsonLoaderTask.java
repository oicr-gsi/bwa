package ca.on.oicr.pde.seqprodreporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;
import android.util.TimeFormatException;

public class JsonLoaderTask extends AsyncTask<Void, Void, List<Report>> {

    private WeakReference<ReportListFragment> mParent;
    private String TYPE;
    
	public JsonLoaderTask(ReportListFragment parent, String type) {
		super();
		mParent = new WeakReference<ReportListFragment>(parent);
		TYPE = type;
	}

	
	@Override
	protected List<Report> doInBackground(Void... params) {
		List<Report> report = null;
		try {
		  report = getReportsFromFile();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		return report;
		
	}
	

	@Override
	protected void onPostExecute(List<Report> result) {

		if (null != result && null != mParent.get()) {
			mParent.get().addLocalReports(result);
		}
	}

	private List<Report> getReportsFromFile(Void... params) throws IOException {

		Resources r = mParent.get().getResources();
		//TODO we need to change this so that we read from a designated file
		InputStream fis =  r.openRawResource(R.raw.test);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		String jsonLine = "";
        String jString  = "";
		
		while (null != (jString = br.readLine())) {
			jsonLine = jsonLine.concat(jString);
		}

		br.close();
		return getRecordsFromJSON(jsonLine, this.TYPE);
	}
	
	public static List<Report> getRecordsFromJSON (String JsonString, String type) {
		List<Report> result = new ArrayList<Report>();
		try {
			JSONObject object = (JSONObject) new JSONTokener(JsonString)
					.nextValue();
			JSONArray Reports = object.getJSONArray(type);
			for (int i = 0; i < Reports.length(); i++) {
				JSONObject tmp = (JSONObject) Reports.get(i);

				String lmTime = tmp.getString("lmtime").replaceAll("-", ":").replaceAll(":","");
				//2014-03-21 14:32:23.729
				try {
				  String parsable = lmTime.substring(0, lmTime.lastIndexOf(".")).replace(" ", "T");
				  Time recordTime = new Time();
				  recordTime.parse(parsable);
				  //TODO compare to the time of the last update
				} catch (TimeFormatException tfe) {
				  Log.e(ReporterActivity.TAG,"");
				}
				
				Report newReport = new Report(
						tmp.getString("sample"),
						tmp.getString("workflow"),
						tmp.getString("version"),
						tmp.getString("crtime"),
						tmp.getString("lmtime"));
				if (type.equals(ReporterActivity.types[2])) {
					String p = tmp.getString("progress");
					if (null !=p && !p.isEmpty())
						newReport.setrProgress(p);
				}
					
				result.add(newReport);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}


}
