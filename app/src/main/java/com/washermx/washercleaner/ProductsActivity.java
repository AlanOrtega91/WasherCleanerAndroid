package com.washermx.washercleaner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.washermx.washercleaner.model.AppData;
import com.washermx.washercleaner.model.Product;

import java.util.List;

public class ProductsActivity extends AppCompatActivity implements View.OnClickListener {

    private Handler handler;
    SharedPreferences settings;
    String token;
    List<Product> products;
    GridView productsGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        initValues();
        initView();
        initThreads();
    }

    private void initValues() {
        settings = getSharedPreferences(AppData.FILE, 0);
        token = settings.getString(AppData.TOKEN,null);
    }

    private void initView() {
        productsGrid = (GridView) findViewById(R.id.productsGrid);
        configureActionBar();
    }


    private void configureActionBar() {
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) {
            optionsTitleBar.setDisplayShowHomeEnabled(false);
            optionsTitleBar.setDisplayShowCustomEnabled(true);
            optionsTitleBar.setDisplayShowTitleEnabled(false);
            optionsTitleBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            optionsTitleBar.setCustomView(R.layout.titlebar_menu);
            Toolbar parent =(Toolbar) optionsTitleBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0,0);
        }
        TextView menuButton = (TextView)findViewById(R.id.menuButton);
        TextView menuTitle = (TextView)findViewById(R.id.menuTitle);
        if (menuTitle != null) {
            menuTitle.setText(R.string.products_title);
        }
        if (menuButton != null) {
            menuButton.setText(R.string.menu_button);
            menuButton.setOnClickListener(this);
        }
    }

    private void initThreads() {
        handler = new Handler(Looper.getMainLooper());
        Thread readUserThread = new Thread(new Runnable() {
            @Override
            public void run() {
                readProducts();
            }
        });
        readUserThread.start();
    }

    private void readProducts() {
        try {
            products = Product.getProducts(token);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    populateProductListView();
                }
            });
        } catch (Product.errorGettingProducts e) {
            postAlert("Error leyendo información de productos");
        } catch (Product.noSessionFound e){
            postAlert(getString(R.string.session_error));
            changeActivity(MainActivity.class,true);
            finish();
        }
    }

    private void populateProductListView() {
        productsGrid.setAdapter(new ProductsAdapter());
    }

    private void postAlert(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ProductsAdapter extends ArrayAdapter<Product> {
        ProductsAdapter()
        {
            super(ProductsActivity.this,R.layout.product_row,products);
        }

        @Override
        @NonNull public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.product_row, parent, false);
            }
            try {
                Product product = products.get(position);
                TextView amount = (TextView)itemView.findViewById(R.id.amount);
                TextView name = (TextView)itemView.findViewById(R.id.name);
                ImageView image = (ImageView)itemView.findViewById(R.id.productImage);
                ImageView eco = (ImageView)itemView.findViewById(R.id.eco);
                ImageView traditional = (ImageView)itemView.findViewById(R.id.traditional);
                amount.setText(getString(R.string.percentage,Float.parseFloat(product.cantidad)/100));
                name.setText(product.name);
                image.setImageDrawable(checkForImage(product.id,eco,traditional));
                return itemView;
            } catch (Exception e){
                return itemView;
            }
        }

        private Drawable checkForImage(String id, ImageView eco, ImageView traditional){
            Drawable image = null;
            switch (id) {
                case "1":
                    image = ContextCompat.getDrawable(getApplicationContext(),R.drawable.product01);
                    traditional.setVisibility(View.INVISIBLE);
                    eco.setVisibility(View.VISIBLE);
                    break;
                case "2":
                    image = ContextCompat.getDrawable(getApplicationContext(),R.drawable.product02);
                    traditional.setVisibility(View.VISIBLE);
                    eco.setVisibility(View.INVISIBLE);
                    break;
                case "3":
                    image = ContextCompat.getDrawable(getApplicationContext(),R.drawable.product03);
                    traditional.setVisibility(View.VISIBLE);
                    eco.setVisibility(View.VISIBLE);
                    break;
                case "4":
                    image = ContextCompat.getDrawable(getApplicationContext(),R.drawable.product03);
                    traditional.setVisibility(View.VISIBLE);
                    eco.setVisibility(View.VISIBLE);
                    break;
                case "5":
                    image = ContextCompat.getDrawable(getApplicationContext(),R.drawable.product03);
                    traditional.setVisibility(View.VISIBLE);
                    eco.setVisibility(View.VISIBLE);
                    break;
                case "6":
                    image = ContextCompat.getDrawable(getApplicationContext(),R.drawable.product03);
                    traditional.setVisibility(View.VISIBLE);
                    eco.setVisibility(View.VISIBLE);
                    break;
                case "7":
                    image = ContextCompat.getDrawable(getApplicationContext(),R.drawable.product04);
                    traditional.setVisibility(View.VISIBLE);
                    eco.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
            return image;
        }
    }

    private void changeActivity(Class activity, Boolean clear) {
        Intent intent = new Intent(getBaseContext(), activity);
        if (clear) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
