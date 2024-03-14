package longpdph30373.poly.executerservicelab.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import longpdph30373.poly.executerservicelab.MainActivity;
import longpdph30373.poly.executerservicelab.R;
import longpdph30373.poly.executerservicelab.models.User;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Activity activity;
    List<User> list;
    Context context;
    ExecutorService executorService;
    OkHttpClient client;
    String apAddres = "http://192.168.103.36:9999";

    public UserAdapter(List<User> list, Context context, ExecutorService executorService, OkHttpClient client, Activity activity) {
        this.list = list;
        this.context = context;
        this.executorService = executorService;
        this.client = client;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        User user = list.get(position);
        holder.textName.setText(user.name);
        holder.textPrice.setText("" + user.price);
        holder.textBrand.setText(user.brand);
        holder.textEdit.setOnClickListener(view -> {
//            Toast.makeText(context, "id: " + user.id, Toast.LENGTH_SHORT).show();
            showAddDialog(user, holder.getAdapterPosition());
        });
        holder.textDelete.setOnClickListener(view -> {
//            Toast.makeText(context, "id: " + user.id, Toast.LENGTH_SHORT).show();
            showDeleteConfirmationDialog(user, holder.getAdapterPosition());
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPrice, textBrand, textEdit, textDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textBrand = itemView.findViewById(R.id.textBrand);
            textDelete = itemView.findViewById(R.id.textDelete);
            textEdit = itemView.findViewById(R.id.textEdit);
        }
    }

    public void deleteData(final String id) {
        executorService = Executors.newSingleThreadExecutor();
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
                        executorService.shutdown();
                    } else {
                        Log.d("ReponseServer", "Xóa dữ liệu thất bại");
                        executorService.shutdown();
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

    //update
    public void update(final String baseUrl, final String id, final String newName, final int newPrice, final String newBrand, final MainActivity.ResponseListener listener) {
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

    public void showAddDialog(User user, int position) {

            Dialog dialog = new Dialog(activity);
            dialog.setContentView(R.layout.dialog_add);

            EditText editTextName = dialog.findViewById(R.id.editTextName);
            EditText editTextPrice = dialog.findViewById(R.id.editTextPrice);
            EditText editTextBrand = dialog.findViewById(R.id.editTextBrand);

            Button buttonAdd = dialog.findViewById(R.id.buttonAdd);
            Button buttonCancel = dialog.findViewById(R.id.buttonCancel);
            buttonAdd.setText("Update");

            editTextName.setText(user.name);
            editTextPrice.setText("" + user.price);
            editTextBrand.setText(user.brand);

            buttonAdd.setOnClickListener(v -> {
                if (editTextName.getText().toString().equals("") || editTextPrice.getText().toString().equals("") || editTextBrand.getText().toString().equals("")) {
                    Toast.makeText(context, "Hãy nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                }
                if (!editTextName.getText().toString().equals("") && !editTextPrice.getText().toString().equals("") && !editTextBrand.getText().toString().equals("")) {
                    String name = editTextName.getText().toString();
                    int price = Integer.parseInt(editTextPrice.getText().toString());
                    String brand = editTextBrand.getText().toString();

                    update(apAddres, user.id, name, price, brand, new MainActivity.ResponseListener() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("ExecuterService", response);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                    list.set(position, new User(user.id, name, price, brand));
                    notifyItemChanged(position);
                    dialog.dismiss();
                }
                // Xử lý dữ liệu ở đây (ví dụ: lưu vào cơ sở dữ liệu)


            });

            buttonCancel.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
    }

    public void showDeleteConfirmationDialog(User user, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa " + user.getName() + " không?");

        builder.setPositiveButton("Xóa", (dialog, which) -> {
            // Xử lý xóa ở đây
            deleteData(user.id);
            list.remove(position);
            notifyItemRemoved(position);
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
