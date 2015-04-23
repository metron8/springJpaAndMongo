/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package sample.data.jpa;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpMethod;
import sample.data.jpa.service.*;
import sample.data.jpa.domain.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SampleDataRestApplication implements CommandLineRunner{

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private AttractionsRepository attractions;

	@Autowired
	private CityRepository city;

	@Autowired
	private CustomerRepository customer;

	@Override
	public void run(String... args) throws Exception {
		
		this.attractions.deleteAll();

		this.accountRepository.deleteAll();
		this.accountRepository.save(new Account("egor", "1234"));

		// save a couple of customers
		this.attractions.save(new Attractions("БигБен", "БИГБЕН"));//как в конструкторе класса Attractions
		this.attractions.save(new Attractions("Мост", "Мост1"));
		this.attractions.save(new Attractions("Башня", "Башня1"));

		this.city.save(new City("Атланта", "США", "33.7489, -84.3879", "GA"));
		this.city.save(new City("Atlanta", "USA", "33.7489, -84.3879", "GA"));

		this.customer.save(new Customer("Alice", "Smith"));
		System.out.println("Account user");
		Account a = accountRepository.findByUsername("egor");
		System.out.println(a.getUsername());

		//mongoOperation.save(user);

		// fetch all customers
//		System.out.println("Attractions found with findAll():");
//		System.out.println("-------------------------------");
//		for (Attractions attraction : this.attractions.findAll()) {
//			System.out.println(attraction);
//		}
//		System.out.println();
/*
		System.out.println("Customer found with findByDescription('Мост1'):");
		System.out.println("--------------------------------");
		System.out.println(this.attractions.findByDescription("Мост1"));

		System.out.println();
		System.out.println("City found with findByMap('33.7489, -84.3879'):");
		System.out.println(this.city.findByMap("33.7489, -84.3879"));

		System.out.println();
		System.out.println("City found findByNameAndMap:");
		System.out.println(this.city.findByNameAndMap("Атланта","33.7489, -84.3879"));

		System.out.println();
		System.out.println(this.city.findByNameAndCountryAllIgnoringCase("London","UK"));*/

		System.out.println("City found findByNameLike:");
		for (City city: this.city.findByNameLike("Atlanta")){
			System.out.println(city);
		}

	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SampleDataRestApplication.class, args);
	}

}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	AccountRepository accountRepository;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService());
	}

	@Bean
	UserDetailsService userDetailsService() {
		return new UserDetailsService() {

			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				Account account = accountRepository.findByUsername(username);
				if(account != null) {
					return new User(account.getUsername(), account.getPassword(), true, true, true, true,
							AuthorityUtils.createAuthorityList("USER"));
				} else {
					throw new UsernameNotFoundException("could not find the user '"
							+ username + "'");
				}
			}
		};
	}
}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.httpBasic().and()
				.authorizeRequests()
				.antMatchers(HttpMethod.GET, "/attractions").hasRole("ADMIN")
				.antMatchers(HttpMethod.PUT, "/customers/**").hasRole("ADMIN")
				.antMatchers(HttpMethod.PATCH, "/hotels/**").hasRole("ADMIN").and()
				.csrf().disable();
	}
}
