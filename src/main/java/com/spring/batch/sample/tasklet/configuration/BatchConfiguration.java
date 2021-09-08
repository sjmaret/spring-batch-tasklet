package com.spring.batch.sample.tasklet.configuration;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.spring.batch.sample.tasklet.listener.EmpJobExecutionListener;
import com.spring.batch.sample.tasklet.model.Employee;
import com.spring.batch.sample.tasklet.model.Profile;
import com.spring.batch.sample.tasklet.processor.EmployeeItemProcessor;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class BatchConfiguration {
	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	public FlatFileItemReader<Employee> reader() {
		return new FlatFileItemReaderBuilder<Employee>()
		  .name("employeeItemReader")		
		  .resource(new ClassPathResource("employees.csv"))
		  .delimited()
		  .names(new String[]{ "empCode", "empName", "expInYears" })
		  .fieldSetMapper(new BeanWrapperFieldSetMapper<Employee>() {{
			   setTargetType(Employee.class);
		  }})
		  .linesToSkip(1)
		  .build();
	} 	

	@Bean
	public JdbcBatchItemWriter<Profile> writer(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Profile>()
		   .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Profile>())
		   .sql("INSERT INTO profile (empCode, empName, profileName) VALUES (:empCode, :empName, :profileName)")
		   .dataSource(dataSource)
		   .build();
	}

	@Bean
	public ItemProcessor<Employee, Profile> processor() {
		return new EmployeeItemProcessor();
	}

	@Bean
	public Job createEmployeeJob(EmpJobExecutionListener listener, Step step1) {
		return jobBuilderFactory
		  .get("createEmployeeJob")
		  .incrementer(new RunIdIncrementer())
		  .listener(listener)
		  .flow(step1)
		  .end()
		  .build();
	}

	@Bean
	public Step step1(ItemReader<Employee> reader, ItemWriter<Profile> writer,
			ItemProcessor<Employee, Profile> processor) {
		 return stepBuilderFactory
		   .get("step1")
		   .<Employee, Profile>chunk(5)
		   .reader(reader)
		   .processor(processor)
		   .writer(writer)
		   .build();
	}

	/*
	 * @Bean public DataSource dataSource() { HikariDataSource dataSource = new
	 * HikariDataSource(); dataSource.setDriverClassName("org.h2.Driver");
	 * dataSource.setJdbcUrl("jdbc:h2:tcp://localhost/~/mydb");
	 * dataSource.setUsername("sa"); dataSource.setPassword(""); return dataSource;
	 * }
	 */

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
}