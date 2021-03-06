package com.junjange.myapplication.network

import com.google.gson.JsonObject
import com.junjange.myapplication.data.*
import com.junjange.myapplication.utils.API
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Body
import retrofit2.http.POST




interface PollsInterface {
    @POST(API.POST_SIGN_UP)  //연산 지정(post,get 등등)
    fun postSignUp( //body로 들어갈 필드 지정
        @Body signUpData: SignUp
    ): Call<SignUpReponse>

    @POST(API.POST_CHECK_NICK)
    fun postCheckNick(
        @Body NickData: NickData
    ): Call<CheckNickData>
    @GET(API.GET_POST_MYPAGE)
    fun getMyPageGet (): Call<MyPage>

    @POST(API.POST_SIGN_IN)
    fun postSignIn (): Call<MyPage>

    @POST(API.GET_POST_MYPAGE)
    fun postMyPageEdit (
        @Body myPageEditData: MyPageEdit
    ): Call<SignUpReponse>

    @POST(API.POST_POLL_ADD)
    fun postAddPoll (
        @Body NewPollData : NewPoll
    ): Call<NewPollResponse>

    // 전체 투표 조회
    @GET(API.GET_ALL_POLLS)
    suspend fun getAllPolls(): Response<Polls>

    // 핫 투표 조회
    @GET(API.GET_HOT_POLLS)
    suspend fun getHotPolls(): Response<HotPolls>

    // 투표 보기
    @GET(API.GET_VIEW_POLLS)
    suspend fun getViewPolls(
        @Path("pollId") pollId : Int
    ): Response<ViewPolls>

    // 빠른 투표 보기
    @GET(API.GET_QUICK_POLLS)
    suspend fun getQuickPolls(): Response<QuickPolls>

    // 해시태그 이름 검색
    @GET(API.GET_HASHTAG_NAME)
    suspend fun getHashtagName(
        @Path("keyword") keyword : String
    ): Response<HashtagName>

    // 해시태그 검색
    @GET(API.GET_HASHTAG)
    suspend fun getHashtag(
        @Path("hashtagId") hashtagId : Int
    ): Response<Hashtag>

    // 댓글 검색
    @GET(API.GET_COMMENT)
    suspend fun getComments(
        @Path("pollId") pollId : Int
    ): Response<Comment>

    // 댓글 하기
    @POST(API.POST_COMMENT)
    suspend fun postComment(
        @Body postCommentItem: PostCommentItem
    ): Response<JsonObject>


    // 투표 하기
    @POST(API.POST_BALLOT)
    suspend fun ballot(
        @Body postBallotItem: PostBallotItem

    ): Response<Ballot>

    // 재투표 하기
    @POST(API.POST_REVOTE)
    suspend fun reVote(
        @Body postBallotItem: PostBallotItem

    ): Response<Ballot>

    // 투표 삭제하기
    @DELETE(API.DELETE_POLLS)
    suspend fun pollDelete(
        @Path("pollId") pollId : Int
    ): Response<JsonObject>

    // My Polls
    @GET(API.MY_POLLS)
    suspend fun getMyPolls(): Response<MyPolls>

    // My Ballot
    @GET(API.MY_BALLOT)
    suspend fun getMyBallot(): Response<MyBallot>

    // stat
    @POST(API.POST_STAT)
    suspend fun getStat(
        @Body statReqItem: StatReqItem

    ): Response<Stat>

    @Multipart
    @POST(API.POST_IMAGE)
    fun postImage (
        @Part file: MultipartBody.Part?,
        @Part ("poll_id") poll_id: RequestBody,
        @Part ("item_num") item_num: RequestBody
    ): Call<JsonObject>
}