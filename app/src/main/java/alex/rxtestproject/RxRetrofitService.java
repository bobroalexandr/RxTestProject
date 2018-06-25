package alex.rxtestproject;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RxRetrofitService {

    int ITEMS_ON_PAGE = 10;

    @GET("/search/repositories?per_page=10")
    Observable<Response<ReposResponse>> getRepositories(@Query("q") String query, @Query("page") int page);
}
