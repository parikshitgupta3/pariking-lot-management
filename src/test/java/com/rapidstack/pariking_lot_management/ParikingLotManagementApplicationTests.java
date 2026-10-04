package com.rapidstack.pariking_lot_management;

import com.rapidstack.pariking_lot_management.infrastructure.persistence.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Boots the full application context against a real PostgreSQL (via
 * Testcontainers) so the datasource, JPA, and bean wiring — including the
 * persistence adapter satisfying the ticket repository port — are verified
 * end to end.
 */
class ParikingLotManagementApplicationTests extends AbstractPostgresIntegrationTest {

	@Test
	void contextLoads() {
	}

}
