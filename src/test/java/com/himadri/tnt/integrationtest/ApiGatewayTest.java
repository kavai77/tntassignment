package com.himadri.tnt.integrationtest;

import com.himadri.tnt.entity.Country;
import com.himadri.tnt.entity.OrderNumber;
import com.himadri.tnt.entity.PricingApiResponse;
import com.himadri.tnt.entity.Product;
import com.himadri.tnt.entity.ShipmentApiResponse;
import com.himadri.tnt.entity.TrackApiResponse;
import com.himadri.tnt.entity.TrackingStatus;
import com.himadri.tnt.gateway.ApiGateway;
import com.himadri.tnt.gateway.DefaultApiGateway;
import com.himadri.tnt.gateway.ThrottlingApiGateway;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "INTEGRATIONTEST", matches = "enabled")
class ApiGatewayTest {
	@Autowired
	private ApplicationContext applicationContext;

	@ParameterizedTest
	@ValueSource(classes = {DefaultApiGateway.class, ThrottlingApiGateway.class})
	void shipmentAPITest(Class<? extends ApiGateway> apiGatewayClazz) throws Exception {
		ApiGateway apiGateway = applicationContext.getBean(apiGatewayClazz);
		ShipmentApiResponse actualShipment = apiGateway.queryApi(ShipmentApiResponse.class, "109347263,123456891")
			.block();

		Map<OrderNumber, List<Product>> orderNumberToProductListMap = actualShipment.getOrderNumberToProductListMap();
		assertEquals(2, orderNumberToProductListMap.size());
		assertTrue(orderNumberToProductListMap.containsKey(new OrderNumber("109347263")));
		assertTrue(orderNumberToProductListMap.containsKey(new OrderNumber("123456891")));
		assertEquals(3, orderNumberToProductListMap.get(new OrderNumber("109347263")).size());
		assertEquals(1, orderNumberToProductListMap.get(new OrderNumber("123456891")).size());
	}

	@ParameterizedTest
	@ValueSource(classes = {DefaultApiGateway.class, ThrottlingApiGateway.class})
	void trackingAPITest(Class<? extends ApiGateway> apiGatewayClazz) throws Exception {
		ApiGateway apiGateway = applicationContext.getBean(apiGatewayClazz);
		TrackApiResponse tracking = apiGateway.queryApi(TrackApiResponse.class, "109347263,123456891")
			.block();

		Map<OrderNumber, TrackingStatus> orderNumberToTrackingStatusMap = tracking.getOrderNumberToTrackingStatusMap();
		assertEquals(2, orderNumberToTrackingStatusMap.size());
		assertTrue(orderNumberToTrackingStatusMap.containsKey(new OrderNumber("109347263")));
		assertTrue(orderNumberToTrackingStatusMap.containsKey(new OrderNumber("123456891")));
		assertNotNull(orderNumberToTrackingStatusMap.get(new OrderNumber("109347263")));
		assertNotNull(orderNumberToTrackingStatusMap.get(new OrderNumber("123456891")));
	}

	@ParameterizedTest
	@ValueSource(classes = {DefaultApiGateway.class, ThrottlingApiGateway.class})
	void pricingAPITest(Class<? extends ApiGateway> apiGatewayClazz) throws Exception {
		ApiGateway apiGateway = applicationContext.getBean(apiGatewayClazz);
		PricingApiResponse pricing = apiGateway.queryApi(PricingApiResponse.class, "NL,CN")
			.block();

		Map<Country, BigDecimal> countryToPriceMap = pricing.getCountryToPriceMap();
		assertEquals(2, countryToPriceMap.size());
		assertTrue(countryToPriceMap.containsKey(new Country("NL")));
		assertTrue(countryToPriceMap.containsKey(new Country("CN")));
		assertNotNull(countryToPriceMap.get(new Country("CN")));
		assertNotNull(countryToPriceMap.get(new Country("CN")));
	}
}
