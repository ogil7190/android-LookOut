package com.mycampusdock.dock.objects;

import com.mycampusdock.dock.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Event extends RealmObject {
    @PrimaryKey
    @Index
    String eventId;
    private String title;
    private String description;
    private String start;
    private String end;
    private String teamSize;
    private RealmList<RealmData> posters = new RealmList<>();
    private String category;
    private String tags;
    private String audience;
    private String creatorIcon;
    private RealmList<RealmData> coordinators = new RealmList<>();
    private String reach;
    private String createdBy;
    private String belongsTo;
    private long updatedOn;
    private boolean isEnrolled;
    private int banner = R.drawable.event3;
    private boolean isDummy;
    private String venue;
    private String cost;
    private boolean expired;
    private boolean isBookmarked;
    public boolean isRated;
    private boolean isReached;
    private boolean isViewed;
    private int count;

    public Event() {
    }

    public Event(String eventId, String title, String description, String start, String end, String venue, String cost, String teamSize, RealmList<RealmData> posters, String category, String tags, String audience, String creatorIcon, RealmList<RealmData> coordinators, String reach, String createdBy, String belongsTo, long updatedOn, boolean isEnrolled) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.start = start;
        this.end = end;
        this.venue = venue;
        this.teamSize = teamSize;
        this.posters = posters;
        this.category = category;
        this.cost = cost;
        this.tags = tags;
        this.audience = audience;
        this.creatorIcon = creatorIcon;
        this.coordinators = coordinators;
        this.reach = reach;
        this.createdBy = createdBy;
        this.belongsTo = belongsTo;
        this.updatedOn = updatedOn;
        this.isEnrolled = isEnrolled;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isRated() {
        return isRated;
    }

    public void setRated(boolean rated) {
        isRated = rated;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    public Event parseFromJson(JSONObject obj) throws JSONException {
        this.eventId = obj.getString("event_id");
        this.title = obj.getString("event_title");
        this.description = obj.getString("event_description");
        this.start = obj.getString("event_start");
        this.end = obj.getString("event_end");
        this.audience = obj.getString("event_audience");
        this.tags = obj.getString("event_tags");
        this.reach = "" + obj.getJSONArray("event_reach").length();
        this.createdBy = obj.getString("creator_name");
        this.belongsTo = obj.getString("belongs_to");
        this.updatedOn = obj.getLong("creation_time");
        this.category = obj.getString("event_category");
        JSONArray mediaArray = obj.getJSONArray("event_media");
        this.venue = obj.getString("event_venue");
        this.teamSize = obj.getString("event_team");
        RealmList<RealmData> list = new RealmList<>();
        JSONObject others = obj.getJSONObject("event_other_details");
        String coordinators[] = others.getString("event_coordinator_names").split(",");
        String contacts[] = others.getString("event_coordinator_contact").split(",");
        RealmList<RealmData> l = new RealmList<>();
        for (int i = 0; i < coordinators.length; i++) {
            l.add(new RealmData(coordinators[i], contacts[i]));
        }
        for (int i = 0; i < mediaArray.length(); i++) {
            String media = mediaArray.getString(i);
            list.add(new RealmData(media, null));
        }
        this.coordinators = l;
        this.posters = list;
        this.isDummy = false;
        return this;
    }

    public boolean isReached() {
        return isReached;
    }

    public void setReached(boolean reached) {
        isReached = reached;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isExpired() {

        return expired;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getVenue() {
        return venue;
    }

    public String getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getTeamSize() {
        return teamSize;
    }

    public List<RealmData> getPosters() {
        return posters;
    }

    public String getCategory() {
        return category;
    }

    public String getTags() {
        return tags;
    }

    public String getAudience() {
        return audience;
    }

    public String getCreatorIcon() {
        return creatorIcon;
    }

    public List<RealmData> getCoordinators() {
        return coordinators;
    }

    public String getReach() {
        return reach;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getBelongsTo() {
        return belongsTo;
    }

    public long getUpdatedOn() {
        return updatedOn;
    }

    public boolean isEnrolled() {
        return isEnrolled;
    }

    public int getBanner() {
        return banner;
    }

    public boolean isDummy() {
        return isDummy;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public void setTeamSize(String teamSize) {
        this.teamSize = teamSize;
    }

    public void setPosters(RealmList<RealmData> posters) {
        this.posters = posters;
    }

    public void setCoordinators(RealmList<RealmData> coordinators) {
        this.coordinators = coordinators;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public void setCreatorIcon(String creatorIcon) {
        this.creatorIcon = creatorIcon;
    }

    public void setReach(String reach) {
        this.reach = reach;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setBelongsTo(String belongsTo) {
        this.belongsTo = belongsTo;
    }

    public void setUpdatedOn(long updatedOn) {
        this.updatedOn = updatedOn;
    }

    public void setEnrolled(boolean enrolled) {
        isEnrolled = enrolled;
    }

    public void setBanner(int banner) {
        this.banner = banner;
    }

    public void setDummy(boolean dummy) {
        isDummy = dummy;
    }

    public boolean isViewed() {
        return isViewed;
    }

    public void setViewed(boolean viewed) {
        isViewed = viewed;
    }
}
