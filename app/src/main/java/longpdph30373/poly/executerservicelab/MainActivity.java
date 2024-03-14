package longpdph30373.poly.executerservicelab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import longpdph30373.poly.executerservicelab.adapters.UserAdapter;
import longpdph30373.poly.executerservicelab.models.User;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ExecutorService executorService;

    public OkHttpClient client = new OkHttpClient();
    public String apAddres = "http://192.168.103.36:9999";

    RecyclerView listItem;
    FloatingActionButton fabAdd;
    UserAdapter userAdapter;
    List<User> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executorService = Executors.newCachedThreadPool();
        listItem = findViewById(R.id.listItem);
        fabAdd = findViewById(R.id.fab);
        list = new ArrayList<>();

//        //Get all user
//        GetAllData getAllData = new GetAllData();
//        getAllData.fetchDataFromApi();

        GetData();

        fabAdd.setOnClickListener(view -> {
            showAddDialog();
        });

        //TEST 3 EXECUTOR SERVICE
////       1.Trường hợp 1: Service chỉ chạy 1 luồng đơn.
//        executorService = Executors.newSingleThreadExecutor();

////        2. Trường hợp 2: Giới hạn cho chạy một số luồng nhất định
//        executorService = Executors.newFixedThreadPool(2);

//        3. Cho chạy đồng thời tất cả các luồng
//        executorService = Executors.newCachedThreadPool();

//        for (int i = 0; i < 5; i++){
//            executorService.execute(new MyRunable("Luồng " + i));
//        }

        /*for(int i =0;i<4;i++){
            executorService.execute(new MyRunable(""+i));
        }*/

        //Get all

//        GetAllData getAllData = new GetAllData();
//        getAllData.fetchDataFromApi();
//        getAllData.shutdown();

//
//        getById(apAddres, "65e7fd214ac84e2262b5177e", new ResponseListener() {
//            @Override
//            public void onResponse(String response) {
//                Log.d("ExecuterService: ", response);
//            }
//
//            @Override
//            public void onError(Exception e) {
//
//            }
//        });

//
//
//        update(apAddres, "65e7fd214ac84e2262b5177e", "Tôi là update", 1111, "update", new ResponseListener() {
//            @Override
//            public void onResponse(String response) {
//                Log.d("ExecuterService", response);
//            }
//
//            @Override
//            public void onError(Exception e) {
//
//            }
//        });

//        DeletePoster deletePoster = new DeletePoster();
//        deletePoster.deleteData("65e7fd214ac84e2262b5177e");
//        deletePoster.shutdown();


    }

    private JSONArray callAPIGetData(String urlString) throws IOException {
        // Tạo URL từ đường dẫn đã cho
        URL url = new URL(urlString);
        // Mở kết nối
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            // Đọc dữ liệu từ kết nối
            InputStream in = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            // Chuyển dữ liệu đọc được thành một JSONArray
            return new JSONArray(result.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            // Đóng kết nối
            urlConnection.disconnect();
        }
    }

    public void postData(final String urlString, final String name, final int price, final String brand, final ResponseListener listener) {
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    // Tạo đối tượng JSON từ dữ liệu
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", name);
                    jsonObject.put("price", price);
                    jsonObject.put("brand", brand);

                    // Gửi dữ liệu
                    DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
                    outStream.writeBytes(jsonObject.toString());
                    outStream.flush();
                    outStream.close();

                    // Đọc phản hồi từ server
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Gửi phản hồi cho listener
                    if (listener != null) {
                        listener.onResponse(response.toString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // Gửi lỗi cho listener
                    if (listener != null) {
                        listener.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        });
    }


    public interface ResponseListener {
        void onResponse(String response);

        void onError(Exception e);
    }

    public class MyRunable implements Runnable {
        String name;

        public MyRunable(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            try {
                Log.d("ExecuterService", name + " dang chạy");
                Thread.sleep(5000);
                Log.d("ExecuterService", name + " chết");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public class MyCallable implements Callable<JSONArray> {

        @Override
        public JSONArray call() throws Exception {
            JSONArray jsonArray = callAPIGetData(apAddres + "/users");
            return jsonArray;
        }
    }

    //update
    public void update(final String baseUrl, final String id, final String newName, final int newPrice, final String newBrand, final ResponseListener listener) {
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(baseUrl + "/user/update/" + id);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    // Tạo đối tượng JSON chứa thông tin cập nhật
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", newName);
                    jsonObject.put("price", newPrice);
                    jsonObject.put("brand", newBrand);

                    // Gửi dữ liệu cập nhật
                    DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
                    outStream.writeBytes(jsonObject.toString());
                    outStream.flush();
                    outStream.close();

                    // Đọc phản hồi từ server
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Gửi phản hồi cho listener
                    if (listener != null) {
                        listener.onResponse(response.toString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // Gửi lỗi cho listener
                    if (listener != null) {
                        listener.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        });
    }

    //DELETE
    public class DeletePoster {
        public void deleteData(final String id) {
//            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Tạo URL cho yêu cầu DELETE
                        String urlDelete = apAddres + "/user/delete/" + id;
                        Log.d("ReponseServer", "Check url: " + urlDelete);


                        // Tạo yêu cầu DELETE
                        Request request = new Request.Builder()
                                .url(urlDelete)
                                .delete()
                                .build();

                        // Thực hiện yêu cầu DELETE
                        Response response = client.newCall(request).execute();

                        // Kiểm tra xem yêu cầu có thành công không
                        if (response.isSuccessful()) {
                            Log.d("ReponseServer", "Xóa dữ liệu thành công id: " + id);
                        } else {
                            Log.d("ReponseServer", "Xóa dữ liệu thất bại");
                        }

                        // Đóng response
                        response.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("ReponseServer", "Lỗi xảy ra khi xóa dữ liệu: " + e.getMessage());
                    }
                }
            });
        }

        public void shutdown() {
            executorService.shutdown();
        }
    }

    //GET ALL
    public class GetAllData {
        public void fetchDataFromApi() {
//            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new Runnable() {
                @Override
                public void run() {

                    String urlGetAll = apAddres + "/users";

                    Request request = new Request.Builder()
                            .url(urlGetAll)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String jsonData = response.body().string();
                                // Xử lý chuỗi JSON ở đây
                                Log.d("ReponseServer", "Data get all: " + jsonData);
                            } else {
                                // Xử lý khi không thành công
                            }
                        }

                        @Override
                        public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                            e.printStackTrace();
                            // Xử lý khi có lỗi
                            Log.d("ReponseServer", "Lỗi xảy ra khi get all " + e.getMessage());
                        }
                    });

                }
            });
        }

        public void shutdown() {
            executorService.shutdown();
        }
    }

    //Get By ID
    public void getById(final String baseUrl, final String id, final ResponseListener listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(baseUrl + "/users/" + id);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    // Đọc phản hồi từ server
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Gửi phản hồi cho listener
                    if (listener != null) {
                        listener.onResponse(response.toString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // Gửi lỗi cho listener
                    if (listener != null) {
                        listener.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        });
    }

    public void showAddDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add);

        EditText editTextName = dialog.findViewById(R.id.editTextName);
        EditText editTextPrice = dialog.findViewById(R.id.editTextPrice);
        EditText editTextBrand = dialog.findViewById(R.id.editTextBrand);

        Button buttonAdd = dialog.findViewById(R.id.buttonAdd);
        Button buttonCancel = dialog.findViewById(R.id.buttonCancel);

        buttonAdd.setOnClickListener(v -> {
            if (editTextName.getText().toString().equals("") || editTextPrice.getText().toString().equals("") || editTextBrand.getText().toString().equals("")) {
                Toast.makeText(this, "Hãy nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
            if (!editTextName.getText().toString().equals("") && !editTextPrice.getText().toString().equals("") && !editTextBrand.getText().toString().equals("")) {
                String name = editTextName.getText().toString();
                int price = Integer.parseInt(editTextPrice.getText().toString());
                String brand = editTextBrand.getText().toString();

                postData(apAddres + "/user/post",
                        name, price, brand, new ResponseListener() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("ExecuterService", response);
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                list.clear();
                GetData();
                dialog.dismiss();
            }
            // Xử lý dữ liệu ở đây (ví dụ: lưu vào cơ sở dữ liệu)


        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public void GetData() {
        try {
            Future<JSONArray> future = executorService.submit(new MyCallable());
            if (future.get() != null) {
                Log.d("ExecuterService", future.get().toString());
                JSONArray jsonArray = null; // jsonString là chuỗi JSON bạn đã cung cấp
                try {
                    jsonArray = new JSONArray(future.get().toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String id = jsonObject.getString("_id");
                        String name = jsonObject.getString("name");
                        int price = jsonObject.getInt("price");
                        String brand = jsonObject.getString("brand");

                        User product = new User(id, name, price, brand);
                        list.add(product);
                    }
                    userAdapter = new UserAdapter(list, getApplicationContext(), executorService, client, MainActivity.this);
                    listItem.setAdapter(userAdapter);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executorService.shutdown();
    }


}