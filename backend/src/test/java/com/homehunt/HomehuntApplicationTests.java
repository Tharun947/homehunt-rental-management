package com.homehunt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homehunt.dto.PropertyResponse;
import com.homehunt.entity.Property;
import com.homehunt.entity.PropertyStatus;
import com.homehunt.entity.PropertyType;
import com.homehunt.entity.Role;
import com.homehunt.entity.User;
import com.homehunt.repository.PropertyRepository;
import com.homehunt.repository.UserRepository;
import com.homehunt.service.PropertyService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HomehuntApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PropertyService propertyService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PropertyRepository propertyRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void publicSearchMatchesApprovedPropertiesByKeyword() {
		User landlord = userRepository.save(User.builder()
				.name("Searchable Landlord")
				.email("search-landlord@example.com")
				.password("not-used")
				.role(Role.LANDLORD)
				.build());
		propertyRepository.save(Property.builder()
				.title("Modern Thomastown Family Home")
				.description("Close to schools and transport")
				.location("Thomastown VIC")
				.price(new BigDecimal("620"))
				.type(PropertyType.HOUSE)
				.status(PropertyStatus.APPROVED)
				.owner(landlord)
				.build());
		propertyRepository.save(Property.builder()
				.title("Hidden Pending Thomastown Flat")
				.description("Older pending rows should still be searchable")
				.location("Thomastown VIC")
				.price(new BigDecimal("500"))
				.type(PropertyType.APARTMENT)
				.status(PropertyStatus.PENDING)
				.owner(landlord)
				.build());

		Page<PropertyResponse> results = propertyService.publicSearch(
				"thomastown", null, null, null, null, PageRequest.of(0, 10));

		assertThat(results.getContent()).extracting(PropertyResponse::title)
				.contains("Modern Thomastown Family Home")
				.doesNotContain("Hidden Pending Thomastown Flat");
		assertThat(results.getContent()).extracting(PropertyResponse::status)
				.contains(PropertyStatus.APPROVED)
				.doesNotContain(PropertyStatus.PENDING);
		assertThat(results.getContent()).extracting(PropertyResponse::landlordEmail)
				.containsOnlyNulls();
	}

	@Test
	void roleBasedHttpAccessIsEnforced() throws Exception {
		String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);
		String tenantToken = register("Tenant User", "tenant-" + suffix + "@example.com", Role.TENANT);
		String landlordToken = register("Landlord User", "landlord-" + suffix + "@example.com", Role.LANDLORD);
		String adminToken = register("Admin User", "admin-" + suffix + "@example.com", Role.ADMIN);

		mockMvc.perform(get("/api/properties"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/properties")
						.contentType(MediaType.APPLICATION_JSON)
						.content(propertyJson("Guest Property")))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/properties")
						.header("Authorization", bearer(tenantToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(propertyJson("Tenant Property")))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/properties")
						.header("Authorization", bearer(landlordToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(propertyJson("Landlord Property")))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/properties/my")
						.header("Authorization", bearer(landlordToken)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/properties/my")
						.header("Authorization", bearer(tenantToken)))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/admin/users")
						.header("Authorization", bearer(tenantToken)))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/admin/users")
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/admin/properties")
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk());
	}

	@Test
	void adminCanUpdateRolesAndDeleteProperties() throws Exception {
		String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);
		String adminToken = register("Admin User", "admin-actions-" + suffix + "@example.com", Role.ADMIN);
		String landlordToken = register("Role Change User", "role-change-" + suffix + "@example.com", Role.LANDLORD);

		JsonNode created = objectMapper.readTree(mockMvc.perform(post("/api/properties")
						.header("Authorization", bearer(landlordToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(propertyJson("Admin Delete Target")))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString());

		long propertyId = created.get("id").asLong();
		long landlordId = jwtUserId(landlordToken);

		mockMvc.perform(put("/api/admin/users/{id}/role", landlordId)
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"role\":\"TENANT\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(delete("/api/admin/properties/{id}", propertyId)
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk());
	}

	@Test
	void adminCanRejectPendingProperty() throws Exception {
		String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);
		String adminToken = register("Reject Admin", "reject-admin-" + suffix + "@example.com", Role.ADMIN);
		String landlordToken = register("Reject Landlord", "reject-landlord-" + suffix + "@example.com", Role.LANDLORD);

		JsonNode created = objectMapper.readTree(mockMvc.perform(post("/api/properties")
						.header("Authorization", bearer(landlordToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(propertyJson("Reject Target")))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString());

		JsonNode rejected = objectMapper.readTree(mockMvc.perform(put("/api/properties/{id}/reject", created.get("id").asLong())
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString());

		assertThat(rejected.get("status").asText()).isEqualTo("REJECTED");
	}

	@Test
	void authenticatedUserCanUpdateProfileAndPassword() throws Exception {
		String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);
		String token = register("Profile User", "profile-" + suffix + "@example.com", Role.TENANT);

		JsonNode updated = objectMapper.readTree(mockMvc.perform(put("/api/account/profile")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Updated Profile","email":"updated-profile-%s@example.com"}
								""".formatted(suffix)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString());

		String updatedToken = updated.get("token").asText();
		assertThat(updated.get("name").asText()).isEqualTo("Updated Profile");
		assertThat(updated.get("email").asText()).isEqualTo("updated-profile-" + suffix + "@example.com");

		mockMvc.perform(put("/api/account/password")
						.header("Authorization", bearer(updatedToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"currentPassword\":\"password123\",\"newPassword\":\"newpassword123\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"updated-profile-%s@example.com","password":"newpassword123"}
								""".formatted(suffix)))
				.andExpect(status().isOk());
	}

	@Test
	void landlordCanViewAllApplicationsForTheirProperties() throws Exception {
		String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);
		String landlordToken = register("All Apps Landlord", "all-apps-landlord-" + suffix + "@example.com", Role.LANDLORD);
		String adminToken = register("All Apps Admin", "all-apps-admin-" + suffix + "@example.com", Role.ADMIN);
		String tenantToken = register("All Apps Tenant", "all-apps-tenant-" + suffix + "@example.com", Role.TENANT);

		JsonNode created = objectMapper.readTree(mockMvc.perform(post("/api/properties")
						.header("Authorization", bearer(landlordToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(propertyJson("All Apps Target")))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString());
		long propertyId = created.get("id").asLong();

		mockMvc.perform(put("/api/properties/{id}/approve", propertyId)
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/applications")
						.header("Authorization", bearer(tenantToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(applicationJson(propertyId, 76000)))
				.andExpect(status().isOk());

		JsonNode applications = objectMapper.readTree(mockMvc.perform(get("/api/applications/landlord")
						.header("Authorization", bearer(landlordToken)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString());

		assertThat(applications.findValuesAsText("propertyTitle")).contains("All Apps Target");
	}

	@Test
	void acceptingOneApplicationRejectsOtherApplicationsForSameProperty() throws Exception {
		String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);
		String landlordToken = register("Application Landlord", "application-landlord-" + suffix + "@example.com", Role.LANDLORD);
		String adminToken = register("Application Admin", "application-admin-" + suffix + "@example.com", Role.ADMIN);
		String tenantOneToken = register("Tenant One", "tenant-one-" + suffix + "@example.com", Role.TENANT);
		String tenantTwoToken = register("Tenant Two", "tenant-two-" + suffix + "@example.com", Role.TENANT);

		JsonNode created = objectMapper.readTree(mockMvc.perform(post("/api/properties")
						.header("Authorization", bearer(landlordToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(propertyJson("Single Approval Target")))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString());
		long propertyId = created.get("id").asLong();

		mockMvc.perform(put("/api/properties/{id}/approve", propertyId)
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk());

		JsonNode first = objectMapper.readTree(mockMvc.perform(post("/api/applications")
						.header("Authorization", bearer(tenantOneToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(applicationJson(propertyId, 72000)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString());
		JsonNode second = objectMapper.readTree(mockMvc.perform(post("/api/applications")
						.header("Authorization", bearer(tenantTwoToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(applicationJson(propertyId, 81000)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString());

		mockMvc.perform(put("/api/applications/{id}/status", first.get("id").asLong())
						.header("Authorization", bearer(landlordToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"ACCEPTED\"}"))
				.andExpect(status().isOk());

		JsonNode applications = objectMapper.readTree(mockMvc.perform(get("/api/applications/property/{id}", propertyId)
						.header("Authorization", bearer(landlordToken)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString());

		assertThat(applications.findValuesAsText("status")).contains("ACCEPTED", "REJECTED");
		assertThat(applications.findValuesAsText("status").stream().filter("ACCEPTED"::equals).count()).isEqualTo(1);
		assertThat(applications.findValuesAsText("id")).contains(first.get("id").asText(), second.get("id").asText());
	}

	private String register(String name, String email, Role role) throws Exception {
		String response = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s","email":"%s","password":"password123","role":"%s"}
								""".formatted(name, email, role.name())))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return objectMapper.readTree(response).get("token").asText();
	}

	private String bearer(String token) {
		return "Bearer " + token;
	}

	private String propertyJson(String title) {
		return """
				{"title":"%s","description":"A secure role test property","location":"Thomastown VIC","price":650,"type":"HOUSE","imageUrl":""}
				""".formatted(title);
	}

	private String applicationJson(long propertyId, int income) {
		return """
				{"propertyId":%d,"income":%d,"notes":"Ready to lease"}
				""".formatted(propertyId, income);
	}

	private long jwtUserId(String token) throws Exception {
		String payload = new String(java.util.Base64.getUrlDecoder().decode(token.split("\\.")[1]));
		return objectMapper.readTree(payload).get("userId").asLong();
	}
}
