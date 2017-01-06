package schoolofnet.com.financial;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpDelete;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.util.EntityUtils;

public class Categories extends Fragment implements View.OnClickListener {

    private Button btnNewCategory;
    private ListView listCategories;
    private HttpClient httpClient = HttpClientBuilder.create().build();
    private ArrayList<Category> categories = new ArrayList<Category>();
    private CustomAdapterCategories cCategories;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle save) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);;

        btnNewCategory = (Button)view.findViewById(R.id.btn_new_category);
        btnNewCategory.setOnClickListener(this);

        listCategories = (ListView)view.findViewById(R.id.listCategories);
        registerForContextMenu(listCategories);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("List Categories");

        try {
            findCategories();
        } catch(IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, new NewCategory());
        ft.commit();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        menu.setHeaderTitle("Options");
        menu.add(0, view.getId(), 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == "Delete") {
            deleteCategory();
        }

        return true;

    }

    public void findCategories() throws IOException, JSONException {
        HttpGet clientGet = new HttpGet("http://morning-beach-86486.herokuapp.com/api/categories");

        clientGet.addHeader("Authorization", "Bearer " + UserSession.getInstance(getContext()).getUserToken());

        HttpResponse response = httpClient.execute(clientGet);

        String json = EntityUtils.toString(response.getEntity());

        JSONObject resData = new JSONObject(json);

        for (int i = 0; i < resData.getJSONArray("data").length(); i++) {
            JSONObject data = resData.getJSONArray("data").getJSONObject(i);

            categories.add(new Category(data.getString("id"), data.getString("name")));
        }

        cCategories =  new CustomAdapterCategories(getContext(), 0, categories);
        listCategories.setAdapter(cCategories);
    }

    public void deleteCategory() {
        View view = (View) getView().getParent();
        TextView txtId = (TextView) view.findViewById(R.id.txt_category_id);

        Integer id = Integer.parseInt(String.valueOf(txtId.getText()));

        HttpDelete clientDelete = new HttpDelete("http://morning-beach-86486.herokuapp.com/api/categories/" + id.toString());
        clientDelete.addHeader("Authorization", "Bearer " + UserSession.getInstance(getContext()).getUserToken());

        HttpResponse response = null;

        try {
            response = httpClient.execute(clientDelete);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 204) {
            updateUI(categories);
            try {
                findCategories();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
        }

    }

    public void updateUI(ArrayList<Category> itens) {
        cCategories.clear();

        if (itens != null) {
            for (Object obj : itens) {
                cCategories.insert((Category) obj, cCategories.getCount());
            }
        }

        cCategories.notifyDataSetChanged();
    }
}
