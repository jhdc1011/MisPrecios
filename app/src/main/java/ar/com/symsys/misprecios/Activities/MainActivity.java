package ar.com.symsys.misprecios.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.sql.SQLClientInfoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import ar.com.symsys.misprecios.R;
import ar.com.symsys.misprecios.ToolsActivity;
import ar.com.symsys.misprecios.storage.Market;
import ar.com.symsys.misprecios.storage.MarketsTableSchema;
import ar.com.symsys.misprecios.storage.Price;
import ar.com.symsys.misprecios.storage.Product;
import ar.com.symsys.misprecios.storage.StorageManager;


public class MainActivity extends ActionBarActivity implements View.OnClickListener{
    private Button                          btnButton;
    private ListView                        listView;
    private TextView                        tvProductInfo;
    private List<HashMap<String, String>>   pricesList = null;
    private String[]                        pricesFrom;
    private int[]                           pricesTo;
    private SimpleAdapter                   pricesAdapter = null;
    private static List<Price>              prices;
    private static Product                  product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StorageManager.getInstance().setContext(getApplicationContext());

        btnButton       = (Button)findViewById(R.id.scan_button);
        tvProductInfo   = (TextView)findViewById(R.id.productInfoTextView);

        btnButton.setOnClickListener(this);
        pricesFrom = new String[]{
                "PRICE",
                "QUANTITY",
                "DATE",
                "MARKET"};
        pricesTo = new int[]{R.id.price, R.id.quantity, R.id.date, R.id.market_name};

        tvProductInfo.setText("");
    }

    @Override
    protected void onResume(){
        super.onResume();
        //Toast.makeText(getApplicationContext(), "Resume", Toast.LENGTH_SHORT).show();

        if(prices != null && prices.size() > 0){

            ShowResults(product.getProductId(), prices);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.scan_button){
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent );
//        List<Price> prices;

        if(scanningResult != null){

            String productId = scanningResult.getContents();

            if( productId != null) {

                product = StorageManager.getInstance().findProductById(productId);

                if( product == null ){
                    Intent toolIntent = new Intent(this, ToolsActivity.class);
                    toolIntent.putExtra(ToolsActivity.ACTION, ToolsActivity.ADD_PRODUCT);
                    toolIntent.putExtra(ToolsActivity.PRODUCT_ID, productId);
                    startActivity(toolIntent);
                }
                else
                {
                    prices = StorageManager.getInstance().findPriceByProductId(productId);

                    if (prices.size() > 0) {
                        ShowResults(productId, prices);
                    }
                    else
                    {
                        Intent addPriceIntent = new Intent(this, AddPriceActivity.class);
                        addPriceIntent.putExtra(AddPriceActivity.PRODUCT_ID, productId);
                        startActivity(addPriceIntent);
                    }
                }
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"No scan data received!", Toast.LENGTH_SHORT).show();
        }
    }
    protected void ShowResults( String productId, List<Price> prices ){
        ListView listView = (ListView)findViewById(R.id.listView);

        try{
            Market market;

            tvProductInfo.setText(StorageManager.getInstance().findProductById(productId).getDescription());

            pricesList = new ArrayList<HashMap<String, String>>();
            for( Price price : prices) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("PRICE", "$"+String.valueOf(price.getBulkPrice()));
                map.put("QUANTITY", String.valueOf(price.getBulkQuantity()));
                map.put("DATE", price.getTimeStamp().format("%Y-%m-%d"));
                market = StorageManager.getInstance().findMarket(price.getMarketId());
                map.put("MARKET", market.getName() + "-" + market.getLocation());
                pricesList.add(map);
            }
            pricesAdapter = new SimpleAdapter(this, pricesList, R.layout.price_list_item_layout, pricesFrom, pricesTo);

            listView.setAdapter(pricesAdapter);


        }
        catch(Exception e){
            e.printStackTrace();
        }

    }
}
