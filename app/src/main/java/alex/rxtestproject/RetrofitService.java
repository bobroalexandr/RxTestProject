package alex.rxtestproject;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitService {

    @GET("/search/repositories?page=0&per_page=10")
    Call<ReposResponse> getRepositories(@Query("q") String query);

}
