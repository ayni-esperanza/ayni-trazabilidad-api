package com.trazabilidad.ayni;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.data-initializer.enabled=false",
        "app.admin.bootstrap-enabled=false",
        "spring.devtools.restart.enabled=false"
})
class AyniApplicationTests {

	@Test
	void contextLoads() {
	}

}
