package com.ajitesh.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ajitesh on 30/11/15.
 */
public class Movie implements Parcelable{
    private int id;
    private String title;
    private String image="http://image.tmdb.org/t/p/w185";
    private String overview;
    private float rating;
    private String release;
    private float popularity;
    private String backdrop="http://image.tmdb.org/t/p/w500";
    public Movie(int id,String title,String image, String overview, float rating, String release, float popularity, String backdrop){
        this.id=id;
        this.title=title;
        this.image+=image;
        this.overview=overview;
        this.rating=rating;
        this.release=release;
        this.popularity=popularity;
        this.backdrop+=backdrop;
    }

    protected Movie(Parcel in) {
        id = in.readInt();
        title = in.readString();
        image = in.readString();
        overview = in.readString();
        rating = in.readFloat();
        release = in.readString();
        popularity = in.readFloat();
        backdrop=in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getTitle()
    {
        return title;
    }
    public String getImage()
    {
        return image;
    }
    public String getOverview()
    {
        return overview;
    }
    public float getRating()
    {
        return rating;
    }
    public String getRelease()
    {
        return release;
    }

    public float getPopularity() {
        return popularity;
    }
    public String getBackdrop(){
        return backdrop;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(image);
        dest.writeString(overview);
        dest.writeFloat(rating);
        dest.writeString(release);
        dest.writeFloat(popularity);
        dest.writeString(backdrop);
    }
}
