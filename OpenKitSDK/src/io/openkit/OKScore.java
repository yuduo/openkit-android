/**
 * Copyright 2012 OpenKit
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.openkit;

import io.openkit.asynchttp.OKJsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class OKScore {
	
	private int OKScoreID;
	private int scoreValue;
	private int OKLeaderboardID;
	private OKUser user;
	private int rank;
	
	public OKScore()
	{
		super();
	}
	
	
	/**
	 * Creates OKScore object from JSON
	 * @param scoreJSON
	 */
	public OKScore(JSONObject scoreJSON)
	{
		super();
		initFromJSON(scoreJSON);
	}
	
	private void initFromJSON(JSONObject scoreJSON)
	{
		try {
			this.OKLeaderboardID = scoreJSON.getInt("leaderboard_id");
			this.OKScoreID = scoreJSON.getInt("id");
			this.scoreValue = scoreJSON.getInt("value");
			this.rank = scoreJSON.getInt("rank");
			this.user = new OKUser(scoreJSON.getJSONObject("user"));
			
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("OpenKit", "Error parsing score JSON: " + e.toString());
		}
		
	}
	
	public int getRank()
	{
		return rank;
	}
	
	public void setRank(int aRank)
	{
		this.rank = aRank;
	}
	
	public int getOKScoreID()
	{
		return OKScoreID;
	}
	
	public void setOKScoreID(int aID)
	{
		this.OKScoreID = aID;
	}
	
	public int getScoreValue()
	{
		return scoreValue;
	}
	
	public void setScoreValue(int aValue)
	{
		this.scoreValue = aValue;
	}
	
	public int getOKLeaderboardID()
	{
		return OKLeaderboardID;
	}
	
	public void setOKLeaderboardID(int aID)
	{
		this.OKLeaderboardID = aID;
	}
	
	public OKUser getOKUser()
	{
		return user;
	}
	
	public void setOKUser(OKUser aUser)
	{
		this.user = aUser;
	}
	
	public interface ScoreRequestResponseHandler
	{
		void onSuccess();
		void onFailure(Throwable error);
	}
	
	public void submitScore(final ScoreRequestResponseHandler responseHandler)
	{
		OKUser currentUser = OKUser.getCurrentUser();

		if(currentUser == null) {
			responseHandler.onFailure(new Throwable("Current user is not logged in. To submit a score, the user must be logged into OpenKit"));
			return;
		}

		try {
			JSONObject scoreJSON = getScoreAsJSON();
			
			JSONObject requestParams = new JSONObject();
			requestParams.put("app_key", OpenKit.getOKAppID());
			requestParams.put("score", scoreJSON);

			OKHTTPClient.postJSON("/scores", requestParams, new OKJsonHttpResponseHandler() {

				@Override
				public void onSuccess(JSONObject object) {
					responseHandler.onSuccess();
				}

				@Override
				public void onSuccess(JSONArray array) {
					//This should not be called, submitting a score should
					// not return an array, so this is an errror case
					responseHandler.onFailure(new Throwable("Unknown error from OpenKit servers. Received an array when expecting an object"));
				}

				@Override
				public void onFailure(Throwable error, String content) {
					responseHandler.onFailure(error);
				}

				@Override
				public void onFailure(Throwable e, JSONArray errorResponse) {
					responseHandler.onFailure(new Throwable(errorResponse.toString()));
				}

				@Override
				public void onFailure(Throwable e, JSONObject errorResponse) {
					responseHandler.onFailure(new Throwable(errorResponse.toString()));
				}
			});

		} catch (JSONException e) {
			responseHandler.onFailure(new Throwable("OpenKit JSON parsing error"));
		}

	}
	
	private JSONObject getScoreAsJSON() throws JSONException
	{
		JSONObject scoreJSON = new JSONObject();
		
		scoreJSON.put("value", this.scoreValue);
		scoreJSON.put("leaderboard_id", this.OKLeaderboardID);
		scoreJSON.put("user_id", OKUser.getCurrentUser().getOKUserID());
		
		return scoreJSON;
	}
	
}
