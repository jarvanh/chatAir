package com.theokanning.openai;

import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatGCompletionRequest;
import com.theokanning.openai.completion.chat.ChatGCompletionResponse;
import com.theokanning.openai.completion.chat.anthropic.ChatACompletionRequest;
import com.theokanning.openai.completion.chat.anthropic.ChatACompletionResponse;
import com.theokanning.openai.edit.EditRequest;
import com.theokanning.openai.edit.EditResult;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.engine.Engine;
import com.theokanning.openai.file.File;
import com.theokanning.openai.finetune.FineTuneEvent;
import com.theokanning.openai.finetune.FineTuneRequest;
import com.theokanning.openai.finetune.FineTuneResult;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.ImageResult;
import com.theokanning.openai.model.Model;
import com.theokanning.openai.moderation.ModerationRequest;
import com.theokanning.openai.moderation.ModerationResult;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

//开头/ 指根，因此它将替换baseUrl后面的任何内容
public interface OpenAiApi {

    @GET("models")
    Single<OpenAiResponse<Model>> listModels();

    @GET("models/{model_id}")
    Single<Model> getModel(@Path("model_id") String modelId);

    @POST("completions")
    Single<CompletionResult> createCompletion(@Body CompletionRequest request);

    @Streaming
    @POST("completions")
    Call<ResponseBody> createCompletionStream(@Body CompletionRequest request);
    
    @POST("chat/completions")
    Single<ChatCompletionResult> createChatCompletion(@Body ChatCompletionRequest request);

    @Streaming
    @POST("chat/completions")
    Call<ResponseBody> createChatCompletionStream(@Body ChatCompletionRequest request);

    @Deprecated
    @POST("engines/{engine_id}/completions")
    Single<CompletionResult> createCompletion(@Path("engine_id") String engineId, @Body CompletionRequest request);

    @POST("edits")
    Single<EditResult> createEdit(@Body EditRequest request);

    @Deprecated
    @POST("engines/{engine_id}/edits")
    Single<EditResult> createEdit(@Path("engine_id") String engineId, @Body EditRequest request);

    @POST("embeddings")
    Single<EmbeddingResult> createEmbeddings(@Body EmbeddingRequest request);

    @Deprecated
    @POST("engines/{engine_id}/embeddings")
    Single<EmbeddingResult> createEmbeddings(@Path("engine_id") String engineId, @Body EmbeddingRequest request);

    @GET("files")
    Single<OpenAiResponse<File>> listFiles();

    @Multipart
    @POST("files")
    Single<File> uploadFile(@Part("purpose") RequestBody purpose, @Part MultipartBody.Part file);

    @DELETE("files/{file_id}")
    Single<DeleteResult> deleteFile(@Path("file_id") String fileId);

    @GET("files/{file_id}")
    Single<File> retrieveFile(@Path("file_id") String fileId);

    @POST("fine-tunes")
    Single<FineTuneResult> createFineTune(@Body FineTuneRequest request);

    @POST("completions")
    Single<CompletionResult> createFineTuneCompletion(@Body CompletionRequest request);

    @GET("fine-tunes")
    Single<OpenAiResponse<FineTuneResult>> listFineTunes();

    @GET("fine-tunes/{fine_tune_id}")
    Single<FineTuneResult> retrieveFineTune(@Path("fine_tune_id") String fineTuneId);

    @POST("fine-tunes/{fine_tune_id}/cancel")
    Single<FineTuneResult> cancelFineTune(@Path("fine_tune_id") String fineTuneId);

    @GET("fine-tunes/{fine_tune_id}/events")
    Single<OpenAiResponse<FineTuneEvent>> listFineTuneEvents(@Path("fine_tune_id") String fineTuneId);

    @DELETE("models/{fine_tune_id}")
    Single<DeleteResult> deleteFineTune(@Path("fine_tune_id") String fineTuneId);

    @POST("images/generations")
    Single<ImageResult> createImage(@Body CreateImageRequest request);

    @POST("images/edits")
    Single<ImageResult> createImageEdit(@Body RequestBody requestBody);

    @POST("images/variations")
    Single<ImageResult> createImageVariation(@Body RequestBody requestBody);

    @POST("moderations")
    Single<ModerationResult> createModeration(@Body ModerationRequest request);

    @Deprecated
    @GET("engines")
    Single<OpenAiResponse<Engine>> getEngines();

    @Deprecated
    @GET("engines/{engine_id}")
    Single<Engine> getEngine(@Path("engine_id") String engineId);

    // -----Gemini-----
//    @POST("v1beta/models/gemini-pro:generateContent?key={api_key}")
    @POST("{version}/models/{model}:generateContent")
    Single<ChatGCompletionResponse> createGChatCompletion(
            @Path("model") String model,
            @Path("version") String version,
//            @Query("key") String api_key,
            @Body ChatGCompletionRequest request);

    @Streaming
    @POST("{version}/models/{model}:streamGenerateContent?alt=sse")
    Call<ResponseBody> createGChatCompletionStream(
            @Path("model") String model,
            @Path("version") String version,
            @Body ChatGCompletionRequest request);

    @POST("messages")
    Single<ChatACompletionResponse> createAChatCompletion(@Body ChatACompletionRequest request);

    @Streaming
    @POST("messages")
    Call<ResponseBody> createAChatCompletionStream(@Body ChatACompletionRequest request);
}
