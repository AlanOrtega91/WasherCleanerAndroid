package com.washermx.washercleaner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.washermx.washercleaner.model.AppData;
import com.washermx.washercleaner.model.Database.DataBase;
import com.washermx.washercleaner.model.ProfileReader;
import com.washermx.washercleaner.model.Service;
import com.washermx.washercleaner.model.User;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends AppCompatActivity implements View.OnClickListener, LocationListener, OnMapReadyCallback {

    /*
    * Menu
     */
    private DrawerLayout drawerLayout;
    private ArrayList<String> navigationItems = new ArrayList<>();
    private ArrayList<Pair<String, Drawable>> listItems = new ArrayList<>();
    private static final int HISTORY = 1;
    private static final int PRODUCTS = 2;
    private static final int LOGOUT = 3;
    User user;


    Handler handler = new Handler(Looper.getMainLooper());
    TextView statusDisplay;
    TextView acceptDisplay;
    TextView cancelDisplay;
    LinearLayout informationLayout;
    TextView rightButton;
    boolean alertSent = true;
    /*
     * Map
     */
    private final static int INTERVAL_IN_MILISECONDS = 100;
    TextView locationText;
    Marker locationMarker;
    Marker serviceMarker;
    LocationManager locationManager;
    GoogleMap map;
    /*
     * Timers
     */
    Timer findRequestsNearbyTimer = new Timer();
    Timer drawPathTimer = new Timer();
    Timer clock;
    /*
     * Service
     */
    SharedPreferences settings;
    Service activeService;
    String idClient;
    List<Service> services = new ArrayList<>();
    String token;
    Thread activeServiceCycleThread;
    Boolean changingStatus = false;

    Boolean noSessionFound = false;

    Boolean faltan5Min = false;

    Location location;

    private static final int ACCESS_FINE_LOCATION = 1;
    private static final String PROVIDER = LocationManager.PASSIVE_PROVIDER;
    Boolean primerUbicacion = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Intent serviceIntent = new Intent(getBaseContext(), LocationServiceInfinite.class);
        Intent firebaseIntent = new Intent(getBaseContext(), FirebaseMessagingService.class);
        startService(serviceIntent);
        startService(firebaseIntent);
        initLocation();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initThreads();
        initValues();
        readUserImage();
        readUserRating();
        AppData.saveInBackground(settings, false);
        initLocation();
    }

    private void readUserRating() {
        int rating = (int) Math.round(user.rating);
        ImageView image = (ImageView) findViewById(R.id.ratingImage);
        if (image == null) {
            return;
        }
        switch (rating) {
            case 0:
                image.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.rating0));
                break;
            case 1:
                image.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.rating1));
                break;
            case 2:
                image.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.rating2));
                break;
            case 3:
                image.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.rating3));
                break;
            case 4:
                image.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.rating4));
                break;
            case 5:
                image.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.rating5));
                break;
            default:
                image.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.rating0));
                break;
        }
    }

    private void readUserImage() {
        TextView headerTitle = (TextView) findViewById(R.id.menuTitle);
        ImageView headerImage = (ImageView) findViewById(R.id.cleanerImage);
        headerTitle.setText(getString(R.string.user_name, user.name, user.lastName));
        if (user.imagePath != null && !user.imagePath.equals("")) {
            headerImage.setImageBitmap(User.readImageBitmapFromFile(user.imagePath));
        }
    }

    @Override
    protected void onPause() {
        Log.i("PAUSED", "App paused");
        AppData.saveInBackground(settings, true);
        cancelTimers();
        locationManager.removeUpdates(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i("DESTROYED", "App closed");
        cancelTimers();
        super.onDestroy();
    }

    private void cancelTimers() {
        if (findRequestsNearbyTimer != null) findRequestsNearbyTimer.cancel();
        if (drawPathTimer != null) drawPathTimer.cancel();
        if (clock != null) clock.cancel();
    }

    @Override
    protected void onStop() {
        Log.i("STOPPED", "App stopped");
        super.onStop();
    }

    private void initValues() {
        settings = getSharedPreferences(AppData.FILE, 0);
        token = settings.getString(AppData.TOKEN, null);
        idClient = settings.getString(AppData.IDCLIENTE, null);
        DataBase db = new DataBase(getBaseContext());
        user = db.readUser();
        activeService = db.getActiveService();
        if (activeService != null) {
            startActiveServiceCycle();
        }
    }

    public void startActiveServiceCycle() {
        if (activeServiceCycleThread == null || !activeServiceCycleThread.isAlive()) {
            activeServiceCycleThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    activeServiceCycle();
                }
            });
            activeServiceCycleThread.start();
        }
    }

    public void activeServiceCycle() {
        DataBase db = new DataBase(getBaseContext());
        while ((activeService = db.getActiveService()) != null) {
            configureActiveServiceView();
            while (!settings.getBoolean(AppData.SERVICE_CHANGED, false)) ;
        }
        configureServiceForDelete();
    }

    private void configureActiveServiceView() {
        switch (activeService.status) {
            case "Accepted":
                configureActiveServiceAccepted(getString(R.string.start));
                break;
            case "Started":
                clock = new Timer();
                String display = getString(R.string.time_remaining) + " -- min";
                configureActiveServiceStarted(display);
                clock.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (activeService == null || activeService.finalTime == null || changingStatus)
                            return;
                        String display;
                        long diff = Service.getDifferenceTimeInMillis(activeService.finalTime);
                        int minutes = (int) diff / 1000 / 60 + 1;
                        if (diff < 0) {
                            display = getString(R.string.finish);
                            if (!alertSent) {
                                if (settings.getBoolean(AppData.IN_BACKGROUND, false))
                                    AlarmNotification.notify(getBaseContext(), getString(R.string.time_ran_out), MapActivity.class);
                                alertSent = true;
                            }
                        } else {
                            alertSent = false;
                            display = getString(R.string.time_remaining) + " " + minutes + " min";
                        }
                        configureActiveServiceStarted(display);
                    }
                }, 1, 1000);
                break;
        }
        AppData.notifyNewData(settings, false);
    }

    private void configureStateLooking() {
        if (activeService != null) {
            serviceMarker.setVisible(true);
            rightButton.setVisibility(View.VISIBLE);
            if (services.size() > 0) {
                if (faltan5Min) {
                    DataBase db = new DataBase(getBaseContext());
                    if (db.getActiveServices().size() < 2) {
                        statusDisplay.setEnabled(true);
                        statusDisplay.setVisibility(View.VISIBLE);
                        acceptDisplay.setVisibility(View.VISIBLE);
                        acceptDisplay.setEnabled(true);
                        acceptDisplay.setText(getString(R.string.accept));
                    } else {
                        statusDisplay.setEnabled(true);
                        statusDisplay.setVisibility(View.VISIBLE);
                        acceptDisplay.setVisibility(View.GONE);
                    }
                } else {
                    statusDisplay.setEnabled(true);
                    statusDisplay.setVisibility(View.VISIBLE);
                    acceptDisplay.setVisibility(View.GONE);
                }

            } else {
                statusDisplay.setEnabled(true);
                statusDisplay.setVisibility(View.VISIBLE);
                acceptDisplay.setVisibility(View.GONE);
            }
        } else {
            serviceMarker.setVisible(false);
            rightButton.setVisibility(View.INVISIBLE);
            acceptDisplay.setVisibility(View.VISIBLE);
            cancelDisplay.setVisibility(View.GONE);
            statusDisplay.setVisibility(View.GONE);
            if (services.size() > 0) {
                acceptDisplay.setEnabled(true);
                acceptDisplay.setText(getString(R.string.accept));
            } else {
                acceptDisplay.setEnabled(false);
                acceptDisplay.setText(getString(R.string.looking));
            }
        }
    }

    private void configureActiveServiceAccepted(final String display) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                rightButton.setVisibility(View.VISIBLE);
                serviceMarker.setVisible(true);
                if (activeService != null)
                    serviceMarker.setPosition(new LatLng(activeService.latitud, activeService.longitud));
                statusDisplay.setEnabled(true);
                statusDisplay.setText(display);
                informationLayout.setVisibility(View.VISIBLE);
                cancelDisplay.setVisibility(View.VISIBLE);
            }
        });
        Thread sendGetGeoLocation = new Thread(new Runnable() {
            @Override
            public void run() {
                getGeoLocation();
            }
        });
        sendGetGeoLocation.start();
    }

    private void configureActiveServiceStarted(final String display) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                rightButton.setVisibility(View.VISIBLE);
                serviceMarker.setVisible(true);
                if (activeService != null)
                    serviceMarker.setPosition(new LatLng(activeService.latitud, activeService.longitud));
                statusDisplay.setEnabled(true);
                statusDisplay.setText(display);
                informationLayout.setVisibility(View.GONE);
                cancelDisplay.setVisibility(View.GONE);
            }
        });
    }


    private void configureServiceForDelete() {
        if (clock != null) clock.cancel();
        if (settings.getString(AppData.MESSAGE, "").equals(getString(R.string.notify_canceled))) {
            if (!settings.getBoolean(AppData.IN_BACKGROUND, false)) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        createAlert(getString(R.string.notify_canceled));
                    }
                });
            }
        } else {
            changeActivity(SummaryActivity.class, false);
        }
        AppData.deleteMessage(settings);
    }

    private void initView() {
        configureMenu();
        configureActionBar();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationText = (TextView) findViewById(R.id.locationText);
        statusDisplay = (TextView) findViewById(R.id.statusDisplay);
        acceptDisplay = (TextView) findViewById(R.id.acceptDisplay);
        cancelDisplay = (TextView) findViewById(R.id.cancelDisplay);
        informationLayout = (LinearLayout) findViewById(R.id.informationLayout);
        if (informationLayout != null) informationLayout.setVisibility(View.GONE);
    }

    private void configureMenu() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView menuList = (ListView) findViewById(R.id.menuList);
        View header = getLayoutInflater().inflate(R.layout.menu_header, menuList, false);
        menuList.addHeaderView(header);
        String[] titles = getResources().getStringArray(R.array.menu_options);
        Collections.addAll(navigationItems, titles);
        listItems.add(Pair.create(titles[0], ContextCompat.getDrawable(getBaseContext(), R.drawable.history_icon)));
        listItems.add(Pair.create(titles[1], ContextCompat.getDrawable(getBaseContext(), R.drawable.products_icon)));
        listItems.add(Pair.create(titles[2], ContextCompat.getDrawable(getBaseContext(), R.drawable.line_white)));
        final MenuAdapter adapter = new MenuAdapter();
        menuList.setAdapter(adapter);
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                decideFragment(position);
            }
        });
    }

    private void initLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(PROVIDER, INTERVAL_IN_MILISECONDS, 1, this);
        } catch (SecurityException e) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
        }
    }

    private void configureActionBar() {
        ActionBar optionsTitleBar = getSupportActionBar();
        if (optionsTitleBar != null) {
            optionsTitleBar.setDisplayShowHomeEnabled(false);
            optionsTitleBar.setDisplayShowCustomEnabled(true);
            optionsTitleBar.setDisplayShowTitleEnabled(false);
            optionsTitleBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            optionsTitleBar.setCustomView(R.layout.titlebar_options);
            Toolbar parent = (Toolbar) optionsTitleBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0, 0);
        }
        TextView leftButton = (TextView) findViewById(R.id.leftButtonOptionsTitlebar);
        rightButton = (TextView) findViewById(R.id.rightButtonOptionsTitlebar);
        TextView title = (TextView) findViewById(R.id.titleOptionsTitlebar);
        leftButton.setText(R.string.account);
        rightButton.setText(R.string.information);
        title.setText(R.string.app_name_display);
        title.setTextColor(Color.rgb(7, 96, 53));
        rightButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
    }

    private void initThreads() {
        findRequestsNearbyTimer = new Timer();
        findRequestsNearbyTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                findRequestsNearby();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        configureStateLooking();
                    }
                });
            }
        }, 0, INTERVAL_IN_MILISECONDS / 10);
    }


    public void onClickChangeStatus(View view) {
        if (activeService.status.equals("Accepted"))
            changeServiceStatus(Service.STARTED, "Started", "EMPEZANDO...");
        else if (activeService.status.equals("Started"))
            changeServiceStatus(Service.FINISHED, "Finished", "TERMINANDO...");
    }

    public void onClickAccept(View view) {
        tryAcceptService();
    }

    private void changeServiceStatus(final int status, final String statusString, String statusMessage) {
        statusDisplay.setText(statusMessage);
        final Thread sendChangeServiceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (changingStatus)
                        return;
                    changingStatus = true;
                    Service.changeServiceStatus(activeService.id, String.valueOf(status), token);
                    changingStatus = false;
                    List<Service> services = new DataBase(getBaseContext()).readServices();
                    for (int i = 0; i < services.size(); i++) {
                        if (services.get(i).id.equals(activeService.id)) {
                            services.get(i).status = statusString;
                            if (statusString.equals("Started")) {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                                Date currenTime = new Date();
                                services.get(i).startedTime = format.format(currenTime.getTime());
                                Calendar date = Calendar.getInstance();
                                date.setTime(currenTime);
                                date.add(Calendar.MINUTE, Integer.valueOf(services.get(i).estimatedTime));
                                services.get(i).finalTime = date.getTime();
                                Log.i("FINAL TIME", services.get(i).finalTime.toString());
                            }
                        }
                    }
                    new DataBase(getBaseContext()).saveServices(services);
                    AppData.saveIdService(settings, activeService.id);
                    AppData.notifyNewData(settings, true);
                } catch (Service.errorChangingStatusRequest e) {
                    postAlert("Error cambiando estado");
                    changingStatus = false;
                } catch (Service.noSessionFound e) {
                    if (!noSessionFound) {
                        noSessionFound = true;
                        postAlert(getString(R.string.session_error));
                        changeActivity(MainActivity.class, true);
                    }
                    finish();
                }
            }
        });
        sendChangeServiceThread.start();
    }

    public void onClickCancel(View view) {
        Thread cancelServiceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                cancelService();
            }
        });
        cancelServiceThread.start();
    }


    private void cancelService() {
        try {
            Service.cancelService(activeService.id, token);
        } catch (Service.errorCancelingRequest e) {
            postAlert("Error canceling request");
        } catch (Service.noSessionFound e) {
            if (!noSessionFound) {
                noSessionFound = true;
                postAlert(getString(R.string.session_error));
                changeActivity(MainActivity.class, true);
            }
            finish();
        }
    }

    private void tryAcceptService() {
        statusDisplay.setText(R.string.accepting);
        Thread sendAcceptServiceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (Service service : services) {
                    try {
                        Service acceptedService = Service.acceptService(service.id, token);
                        List<Service> services = new DataBase(getBaseContext()).readServices();
                        services.add(acceptedService);
                        new DataBase(getBaseContext()).saveServices(services);
                        AppData.saveIdService(settings, acceptedService.id);
                        AppData.notifyNewData(settings, true);
                        startActiveServiceCycle();
                        if (acceptedService.metodoDePago.equals("e")) {
                            handler.post(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            Service servicioActivo = new DataBase(getBaseContext()).getActiveService();
                                            createAlert(getString(R.string.pagoEfectivo,servicioActivo.precioAPagar));
                                        }
                                    }
                            );
                        }
                        return;
                    } catch (Service.errorServiceTaken e) {
                        Log.i("Service", "Error accepting service");
                    } catch (Service.noSessionFound e) {
                        if (!noSessionFound) {
                            noSessionFound = true;
                            postAlert(getString(R.string.session_error));
                            changeActivity(MainActivity.class, true);
                        }
                        finish();
                    }
                }
                postAlert("Could not accept service check products");
            }
        });
        sendAcceptServiceThread.start();
    }

    private void findRequestsNearby() {
        if (token == null || this.location == null) {
            return;
        }
        //TODO: Agregar el cambio para poder aceptar 5 min antes de terminar
        if (activeService == null) {
            try {
                //TODO: tres
                faltan5Min = false;
                int servicesAmount = services.size();
                services = Service.getServices(this.location.getLatitude(), this.location.getLongitude(), token);
                if (servicesAmount == 0 && services.size() != 0) {
                    AlarmNotification.notify(getBaseContext(), getString(R.string.services_found), MapActivity.class);
                }
            } catch (Service.errorGettingServices e) {
                Log.i("SERVICE", "Error getting nearby requests try again later");
            } catch (Service.noSessionFound e) {
                if (!noSessionFound) {
                    noSessionFound = true;
                    postAlert(getString(R.string.session_error));
                    changeActivity(MainActivity.class, true);
                }
                finish();
            }
        } else if (activeService.finalTime != null && ((int) Service.getDifferenceTimeInMillis(activeService.finalTime) / 1000 / 60 + 1) < 5) {
            faltan5Min = true;
            //TODO: dos
        }
    }

    int limit = 0;

    private void getGeoLocation() {
        limit++;
        if (limit > 5) {
            postAlert("Error getting location");
            return;
        }
        Geocoder geocoder;
        List<Address> addresses;
        if (activeService == null)
            return;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(activeService.latitud, activeService.longitud, 1);
            if (addresses.size() < 1)
                return;

            final String address = addresses.get(0).getAddressLine(0);
            final String city = addresses.get(0).getLocality();
            final String state = addresses.get(0).getAdminArea();
            final String country = addresses.get(0).getCountryName();
            final String suburb = addresses.get(0).getSubLocality();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    locationText.setText(address + ", " + suburb + ", " + city + ", " + state + ", " + country);
                    DataBase db = new DataBase(getBaseContext());
                    List<Service> services = db.readServices();
                    int i;
                    for (i = 0; i < services.size(); i++) {
                        if (services.get(i).id.equals(activeService.id))
                            break;
                    }
                    services.get(i).address = address + ", " + suburb + ", " + city + ", " + state + ", " + country;
                    db.saveServices(services);
                }
            });
        } catch (Exception e) {
            Log.i("SERVICE", "Error getting street name");
            getGeoLocation();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        BitmapDrawable cleanerDrawable = (BitmapDrawable) ContextCompat.getDrawable(getBaseContext(), R.drawable.washer_bike);
        Bitmap b = cleanerDrawable.getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 60, 60, false);

        //map.getUiSettings().setMyLocationButtonEnabled(true);
        this.map.setTrafficEnabled(true);
        this.map.getUiSettings().setZoomControlsEnabled(true);
        this.map.getUiSettings().setZoomGesturesEnabled(true);

        serviceMarker = this.map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        serviceMarker.setVisible(false);
        locationMarker = this.map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmapResized)));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.map.setMyLocationEnabled(true);
        }
        LatLng locationLatLng;
        if (location == null) {
            locationLatLng = new LatLng(0, 0);
        } else {
            locationLatLng = new LatLng(this.location.getLatitude(), this.location.getLongitude());
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(locationLatLng, 15);
        this.map.moveCamera(cameraUpdate);
        locationMarker.setPosition(locationLatLng);
    }

    private void decideFragment(int position) {
        switch (position){
            case HISTORY:
                changeActivity(HistoryActivity.class,false);
                return;
            case PRODUCTS:
                changeActivity(ProductsActivity.class,false);
                return;
            case LOGOUT:
                sendLogOut();
                return;
            default:
                break;
        }
    }

    private void sendLogOut() {
        ProfileReader.delete(getBaseContext());
        Intent serviceIntent = new Intent(getBaseContext(), LocationServiceInfinite.class);
        Intent firebaseIntent = new Intent(getBaseContext(), FirebaseMessagingService.class);
        stopService(serviceIntent);
        stopService(firebaseIntent);
        MainActivity.onScreen = true;
        changeActivity(MainActivity.class, true);
        Thread sendLogOutThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    user.sendLogout();
                } catch (User.errorWithLogOut e) {
                    postAlert("Error logging out");
                }
            }
        });
        sendLogOutThread.start();
        finish();
    }

    private class MenuAdapter extends ArrayAdapter<Pair<String,Drawable>> {
        MenuAdapter() { super(MapActivity.this,R.layout.menu_item,R.id.listItemName,listItems); }

        @NonNull
        @Override
        public View getView(int position, View convertView,@NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.menu_item, parent, false);
            }
            try {
                Pair<String,Drawable> item = listItems.get(position);
                Drawable icon = item.second;
                TextView itemName = (TextView)itemView.findViewById(R.id.listItemName);
                ImageView itemImage = (ImageView)itemView.findViewById(R.id.listItemImage);
                if (position < navigationItems.size() - 1) {
                    itemImage.setImageDrawable(icon);
                } else {
                    itemName.setCompoundDrawablesWithIntrinsicBounds(null,icon,null,null);
                }
                itemName.setText(item.first);
                return itemView;
            } catch (Exception e){
                return itemView;
            }
        }
    }

    private void createAlert(String title)
    {
        new AlertDialog.Builder(this)
                .setMessage(title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.leftButtonOptionsTitlebar:
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START);
                else
                    drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.rightButtonOptionsTitlebar:
                changeActivity(InformationActivity.class,false);
                break;
        }
    }

    private void changeActivity(Class activity, Boolean clear) {
        Intent intent = new Intent(getBaseContext(), activity);
        if (clear) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);
    }

    private void postAlert(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    public void onLocationChanged(final Location location) {
        this.location = location;
        long tiempo = SystemClock.elapsedRealtimeNanos() - this.location.getElapsedRealtimeNanos();
        Log.i("Ubicacion","Lat=" + this.location.getLatitude() + "   Lon=" + this.location.getLongitude() + "   Tiempo=" + tiempo/1000/1000/1000/60/60 + "h "+ tiempo/1000/1000/1000/60 + "m "+ tiempo/1000/1000/1000 + "s " + tiempo/1000/1000 + "M " + tiempo/1000 + "u ");


        if (locationMarker != null) {
            locationMarker.setPosition(new LatLng(this.location.getLatitude(), this.location.getLongitude()));
        }
        if (primerUbicacion) {
            primerUbicacion = false;
            LatLng locationLatLng = new LatLng(this.location.getLatitude(), this.location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(locationLatLng, 15);
            this.map.moveCamera(cameraUpdate);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        TextView title = (TextView) findViewById(R.id.titleOptionsTitlebar);
        if (provider.equals(PROVIDER))
        {
            if (status == LocationProvider.OUT_OF_SERVICE || status == LocationProvider.TEMPORARILY_UNAVAILABLE)
            {
                title.setTextColor(Color.RED);
            } else {
                title.setTextColor(Color.rgb(7, 96, 53));
            }
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        TextView title = (TextView) findViewById(R.id.titleOptionsTitlebar);
        if (provider.equals(PROVIDER))
        {
            title.setTextColor(Color.rgb(7, 96, 53));
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(PROVIDER))
        {
            TextView title = (TextView) findViewById(R.id.titleOptionsTitlebar);
            title.setTextColor(Color.RED);
        }
    }

    public void onClickTravel(View view) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr=" + activeService.latitud + "," + activeService.longitud));
        startActivity(intent);
    }
}


