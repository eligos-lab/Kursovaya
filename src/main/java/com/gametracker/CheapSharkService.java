package com.gametracker;

import com.gametracker.GameDeal;
import com.gametracker.Deal;
import com.gametracker.Store;
import com.gametracker.GameDealsResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class CheapSharkService {
    private static final String BASE_URL = "https://www.cheapshark.com/api/1.0";
    private Gson gson = new Gson();
    private Map<String, String> storeNames = new HashMap<>();

    public CheapSharkService() {
        loadStores();
    }

    private void loadStores() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + "/stores");
            try (CloseableHttpResponse response = client.execute(request)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);

                Type storeListType = new TypeToken<List<Store>>(){}.getType();
                List<Store> stores = gson.fromJson(result, storeListType);

                System.out.println("=== ЗАГРУЖЕННЫЕ МАГАЗИНЫ ===");
                for (Store store : stores) {
                    storeNames.put(store.getStoreID(), store.getStoreName());
                    System.out.println("  " + store.getStoreID() + ": " + store.getStoreName());
                }
                System.out.println("Всего загружено магазинов: " + storeNames.size());
                System.out.println("=== КОНЕЦ СПИСКА МАГАЗИНОВ ===");
            }
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке списка магазинов: " + e.getMessage());
        }
    }

    public List<GameDeal> searchGames(String gameName) {
        List<GameDeal> games = new ArrayList<>();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String encodedGameName = gameName.replace(" ", "%20");
            String url = BASE_URL + "/games?title=" + encodedGameName + "&limit=10&exact=0";

            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);

                Type gameListType = new TypeToken<List<GameDeal>>(){}.getType();
                List<GameDeal> parsedGames = gson.fromJson(result, gameListType);

                if (parsedGames != null) {
                    games = parsedGames;
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при поиске игр: " + e.getMessage());
        }

        return games;
    }

    public List<Deal> getGameDeals(String gameID) {
        List<Deal> deals = new ArrayList<>();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = BASE_URL + "/games?id=" + gameID;

            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);

                GameDealsResponse gameDealsResponse = gson.fromJson(result, GameDealsResponse.class);

                if (gameDealsResponse != null && gameDealsResponse.getDeals() != null) {
                    deals = gameDealsResponse.getDeals();
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при получении сделок: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка парсинга: " + e.getMessage());
        }

        return deals;
    }

    // Получение информации о сделке по dealID
    public Deal getDealInfo(String dealID) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = BASE_URL + "/deals?id=" + dealID;

            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);

                Type dealListType = new TypeToken<List<Deal>>(){}.getType();
                List<Deal> deals = gson.fromJson(result, dealListType);

                if (deals != null && !deals.isEmpty()) {
                    return deals.get(0);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при получении информации о сделке: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка парсинга сделки: " + e.getMessage());
        }

        return null;
    }

    public String getStoreName(String storeID) {
        return storeNames.getOrDefault(storeID, "Unknown Store");
    }

    public String getDealLink(String dealID) {
        return "https://www.cheapshark.com/redirect?dealID=" + dealID;
    }

    public String getGameLink(String gameID) {
        return "https://www.cheapshark.com/game/" + gameID;
    }
}