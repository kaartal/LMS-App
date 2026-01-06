Life Management System
Life Management System is a comprehensive desktop application developed in Java Programming Language, designed to provide users with a centralized platform for managing daily life activities. By integrating database management with MongoDB and reporting by the iText library, the system offers a solution for tracking personal growth, health, and productivity.

User Authentication and Personalization
Application experience begins with a secure login and registration system. New users are required to provide their first name, last name, email, and a password that meets specific security criteria. A key feature of the registration process is the ability to select a preferred visual theme. If a user does not choose one, the system applies a default theme.

Through the Account Details section, users can manage their profiles. This module allows for the modification of personal information and passwords, while the email address remains fixed as a unique field. All visual changes are managed by a Session Manager that dynamically updates the interface colors across all modules based on the user's saved preferences.

Financial Management
Finance Tracker is implemented for personal accounting. Users can add transactions by specifying the type, amount, and category. The system includes logic to calculate total income and expenses in real-time, providing a clear view of the user's financial health. Software allow for the filtering of expenses by category, which can then be exported into external documents for further analysis.  

Health and Wellness Tracking
In this system was implemented Sleep Tracker and Fitness Planner modules.

The Sleep Tracker records rest by capturing start and end times. A unique feature of this module is the Yearly PDF Report, which generates a landscape-oriented calendar. This calendar uses color-coded cells to visually represent sleep quality—marking days in red, yellow, or green based on the hours of rest achieved.

Fitness Planner automates the process of workout logging. Beyond simply recording calories and distance, the application uses a custom mathematical model to determine the intensity of a workout. By analyzing the relationship between duration and effort, it labels activities from "Low" to "Very High." Like the sleep module, fitness data is compiled into a yearly PDF activity map, allowing users to track their consistency over months.

Shcool Performance and Habits
For students, the School Planner offers a structured way to monitor school progress. It manages a database of subjects and grades, automatically calculating both individual subject averages and a total cumulative GPA. The UI enhances this data with visual star ratings. Users can generate a professional PDF report card that summarizes their academic standing, while built-in notifications remind them of subjects that are missing grades.

Lastly, the Habit Tracker focuses on long-term discipline. Users define unique habits and mark them as completed each day. The system calculates completion percentages to show progress over time. The interface uses status indicators to show which habits require focus for the current day, and a dedicated PDF export function provides a summary of habit streaks and discipline levels.

Technical Architecture
Project is built on a modular architecture where data transfer objects (Records) are separated from the database logic (Information Transfer) and the graphical interface. This ensures that the code is maintainable and scalable. The integration with MongoDB provides a flexible schema for storing deifferent data types, while the iText library ensures that all user data can be transformed into professional, shareable documents.
