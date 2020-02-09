package tim31.pswisa.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import tim31.pswisa.security.TokenUtils;
import tim31.pswisa.security.auth.RestAuthenticationEntryPoint;
import tim31.pswisa.security.auth.TokenAuthenticationFilter;
import tim31.pswisa.service.LoggingService;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	// Implementacija PasswordEncoder-a koriscenjem BCrypt hashing funkcije.
	// BCrypt po defalt-u radi 10 rundi hesiranja prosledjene vrednosti.
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Autowired
	private LoggingService jwtUserDetailsService;

	// Neautorizovani pristup zastcenim resursima
	@Autowired
	private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	// Definisemo nacin autentifikacije
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
	}

	@Autowired
	TokenUtils tokenUtils;

	// Definisemo prava pristupa odredjenim URL-ovima
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				// komunikacija izmedju klijenta i servera je stateless
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

				// za neautorizovane zahteve posalji 401 gresku
				.exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint).and()

				// svim korisnicima dopusti da pristupe putanjama /auth/**, /h2-console/** i
				// /api/foo
				.authorizeRequests().antMatchers("/register").permitAll().antMatchers("/login").permitAll()
				.antMatchers("/api/foo").permitAll().antMatchers("/getUser").permitAll().antMatchers("/h2-console/**")
				.permitAll()

				// svaki zahtev mora biti autorizovan
				.anyRequest().authenticated().and()

				.cors().and()

				// presretni svaki zahtev filterom
				.addFilterBefore(new TokenAuthenticationFilter(tokenUtils, jwtUserDetailsService),
						BasicAuthenticationFilter.class);

		http.csrf().disable();
		http.headers().frameOptions().disable();

	}

	// Generalna bezbednost aplikacije
	@Override
	public void configure(WebSecurity web) {
		// TokenAuthenticationFilter ce ignorisati sve ispod navedene putanje
		web.ignoring().antMatchers(HttpMethod.POST, "/checkup/addReport", "/checkup/addAppointment",
				"/checkup/addRecipes/*", "/checkup/checkupRequest", "/checkup/update", "/checkup/addDoctors/*",
				"/checkup/getAllQuickApp/*", "/checkup/bookQuickApp/*", "/checkup/patientHistory/*/*",
				"/checkup/scheduleCheckup/*", "/checkup/cancelCheckup/*", "/checkup/infoReport/*",
				"/checkup/updateReport", "/checkUpType/deleteType/*", "/checkUpType/addType", "/requestVacation/*",
				"/updateAdministrator", "/clinic/updateClinic", "/clinic/changeNameOfType", "/clinic/searchOneType/*",
				"/clinic/searchRooms", "/clinic/filterRooms", "/clinic/searchClinic", "/clinic/filterClinic/*",
				"/clinic/clinicDoctors", "/clinic/", "/clinic/deleteRoom/*", "/clinic/addRoom", "/clinic/changeRoom",
				"/clinic/getRevenue", "/clinic/addRooms/*", "/clinic/getSelectedDoctor", "/clinic/allDocsOneClinic/*",
				"/clinic/rateClinic", "/codebook", "/codebook/*", "/sendConfirm", "/activateEmail/*", "/changeDate/*",
				"/notifyDoctors/*", "/notifyPatient/*", "/changePassword", "/register", "/login", "/addAdmin",
				"/bookForPatient", "/deleteDoctor", "/findDoctors", "/updateMedicalWorker", "/addMedicalWorker",
				"/searchDoctors", "/verifyRecipe/*", "/vacationRequest", "/rateMedicalWorker", "/findPatients",
				"/filterPatients", "/addPatient", "/editMedicalRecord", "/");

		web.ignoring().antMatchers(HttpMethod.GET, "/checkup/*", "/checkup/getCheckups/*", "/checkup/getVacations/*",
				"/checkup/getCheckup/*", "/checkUpType/getTypes", "/checkUpType/allTypes",
				"/checkUpType/allTypesOneClicnic/*", "/getAdministrator", "/getRequestForVacation", "/requestsForRoom",
				"/clinic/getClinics", "/clinic/getRooms", "/clinic/getFreeRooms", "/clinic/getDoctors",
				"/clinic/getAllTypes", "/clinic/getAllTypes", "/clinic/getClinic", "/clinic/getClinicRaiting",
				"/clinic/getReportForMonth", "/clinic/getReportForWeek", "/clinic/getClinicsByType/*",
				"/clinic/getRooms/*", "/clinic/roomAvailability/*/*", "/clinic/getDetails/*", "/getUser",
				"/rollingInTheDeep", "/getMedicalWorker", "/canAccessToMedicalRecord/*", "/getRecipes",
				"/getAllDoctors", "/getAllAvailable/*/*/*", "/patientRequests", "/getPatients", "/getPatientProfile/*",
				"/getMedicalRecord/*", "/");

		// web.ignoring().antMatchers(HttpMethod.GET, "/", "/webjars/**", "/*.html",
		// "/favicon.ico", "/**/*.html",
		// "/**/*.css", "/**/*.js");
	}

}
